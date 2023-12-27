package io.gen4s.stage

import org.typelevel.log4cats.slf4j.Slf4jLogger
import org.typelevel.log4cats.Logger

import cats.data.NonEmptyList
import cats.effect.kernel.Async
import cats.effect.std.Console as EffConsole
import cats.implicits.*
import cats.Applicative
import io.gen4s.{RecordsReader, TemplateReader, TemplateValidationError}
import io.gen4s.cli.Args
import io.gen4s.conf.StageConfig
import io.gen4s.core.streams.GeneratorStream
import io.gen4s.core.templating.{OutputValidator, Template}
import io.gen4s.core.templating.TemplateBuilder
import io.gen4s.core.InputRecord
import io.gen4s.generators.SchemaReader
import io.gen4s.outputs.OutputStreamExecutor

import fs2.io.file.Files

trait StageExecutor[F[_]] {
  def exec(): F[Unit]
  def preview(): F[Unit]
}

object StageExecutor {

  def make[F[_]: Async: EffConsole: Files: Logger](name: String, args: Args, conf: StageConfig): F[StageExecutor[F]] =
    Async[F].delay {
      new StageExecutor[F] {
        override def exec(): F[Unit] = {
          for {
            logger <- Slf4jLogger.create[F]
            _      <- Logger[F].info(s"Running [$name] stage.")
            flow   <- generatorFlow(args, conf)
            _      <- OutputStreamExecutor.make[F]().write(args.numberOfSamplesToGenerate, flow, conf.output.writer)
          } yield ()
        }

        override def preview(): F[Unit] = {
          for {
            logger <- Slf4jLogger.create[F]
            _      <- Logger[F].info(s"Running [$name] stage.")
            flow   <- generatorFlow(args, conf)
            _      <- flow.through(prettify(args.prettyPreview)).printlns.compile.drain
          } yield ()
        }

        private def prettify(pretty: Boolean): fs2.Pipe[F, Template, String] =
          in => in.map(t => if (pretty) t.render().asPrettyString else t.render().asString)

        private def validate(builder: TemplateBuilder, validators: Set[OutputValidator]): F[Unit] = {
          val templates = builder.build().map(_.render())
          val errors = validators
            .flatMap {
              case OutputValidator.MissingVars => templates.map(OutputValidator.MissingVars.validate)
              case OutputValidator.JSON        => templates.map(OutputValidator.JSON.validate)
            }
            .filter(_.isInvalid)

          if (errors.nonEmpty) {
            TemplateValidationError(errors.map(e => e.show).toList)
              .raiseError[F, Unit]
          } else {
            Applicative[F].unit
          }
        }

        private def generatorFlow(args: Args, conf: StageConfig): F[fs2.Stream[F, Template]] = {
          for {
            logger <- Slf4jLogger.create[F]
            _      <- logger.info(s"Reading schema from file ${conf.input.schema.getAbsolutePath}")
            schema <- SchemaReader.make[F]().read(conf.input.schema)
            _      <- logger.info(s"Reading template from file ${conf.input.template.getAbsolutePath}")
            sources <- TemplateReader
                         .make[F]()
                         .read(conf.input.template, decodeNewLineAsTemplate = conf.input.decodeNewLineAsTemplate)
            recordsStream <-
              conf.input.csvRecords
                .map(f => RecordsReader.make[F]().read(f))
                .getOrElse(Async[F].pure(List.empty[InputRecord]))

            templateBuilder <- Async[F].delay {
                                 if (recordsStream.isEmpty) {
                                   TemplateBuilder.make(
                                     sources,
                                     schema.generators,
                                     conf.input.globalVars,
                                     conf.output.transformers
                                   )
                                 } else {
                                   TemplateBuilder.ofRecordsStream(
                                     sources,
                                     schema.generators,
                                     conf.input.globalVars,
                                     NonEmptyList.fromListUnsafe(recordsStream),
                                     conf.output.transformers
                                   )
                                 }
                               }
            _ <- validate(templateBuilder, conf.output.validators)
          } yield GeneratorStream.stream[F](args.numberOfSamplesToGenerate, templateBuilder)
        }
      }
    }
}
