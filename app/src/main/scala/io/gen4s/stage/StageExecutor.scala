package io.gen4s.stage

import org.typelevel.log4cats.slf4j.Slf4jLogger

import cats.effect.kernel.Async
import cats.effect.std.Console as EffConsole
import cats.implicits.*
import io.gen4s.cli.Args
import io.gen4s.conf.StageConfig
import io.gen4s.core.streams.GeneratorStream
import io.gen4s.core.templating.Template
import io.gen4s.core.templating.TemplateBuilder
import io.gen4s.generators.SchemaReader
import io.gen4s.outputs.OutputStreamExecutor
import io.gen4s.TemplateReader

import fs2.io.file.Files

trait StageExecutor[F[_]] {
  def exec(): F[Unit]
  def preview(): F[Unit]
}

object StageExecutor {

  def make[F[_]: Async: EffConsole: Files](args: Args, conf: StageConfig): F[StageExecutor[F]] = Async[F].delay {
    new StageExecutor[F] {
      override def exec(): F[Unit] = {
        generatorFlow(args, conf).flatMap { flow =>
          OutputStreamExecutor.make[F]().write(args.numberOfSamplesToGenerate, flow, conf.output.writer)
        }
      }

      override def preview(): F[Unit] = {
        generatorFlow(args, conf).flatMap { stream =>
          stream
            .through(prettify(args.prettyPreview))
            .printlns
            .compile
            .drain
        }
      }

      private def prettify(pretty: Boolean): fs2.Pipe[F, Template, String] =
        in => in.map(t => if (pretty) t.render().asPrettyString else t.render().asString)

      private def generatorFlow(args: Args, conf: StageConfig): F[fs2.Stream[F, Template]] = {
        for {
          logger <- Slf4jLogger.create[F]
          _      <- logger.info(s"Reading schema from file ${conf.input.schema.getAbsolutePath}")
          schema <- SchemaReader.make[F]().read(conf.input.schema)
          _      <- logger.info(s"Reading template from file ${conf.input.template.getAbsolutePath}")
          sources <- TemplateReader
                       .make[F]()
                       .read(conf.input.template, decodeNewLineAsTemplate = conf.input.decodeNewLineAsTemplate)
          templateBuilder <-
            Async[F].delay(TemplateBuilder.make(sources, schema.generators, Nil, conf.output.transformers))
        } yield GeneratorStream.stream[F](args.numberOfSamplesToGenerate, templateBuilder)
      }
    }
  }
}
