package io.gen4s.stage

import org.typelevel.log4cats.slf4j.Slf4jLogger

import cats.effect.kernel.Async
import cats.effect.std.Console as EffConsole
import cats.implicits.*
import io.gen4s.cli.Args
import io.gen4s.conf.StageConfig
import io.gen4s.core.streams.GeneratorStream
import io.gen4s.core.templating.RenderedTemplate
import io.gen4s.core.templating.SourceTemplate
import io.gen4s.core.templating.TemplateGenerator
import io.gen4s.core.GeneratorsSchema
import io.gen4s.core.SchemaReader
import io.gen4s.core.TemplateReader

trait StageExecutor[F[_]] {
  def exec(): F[Unit]
  def preview(): F[Unit]
}

object StageExecutor {

  def make[F[_]: Async: EffConsole](args: Args, conf: StageConfig): F[StageExecutor[F]] = Async[F].delay {
    new StageExecutor[F] {
      override def exec(): F[Unit] = {
        // FIXME: Implement
        generatorStream(args, conf)
          .flatMap(_.printlns.compile.drain)
      }

      override def preview(): F[Unit] = {
        generatorStream(args, conf)
          .flatMap(_.printlns.compile.drain)
      }

      def generatorStream(args: Args, conf: StageConfig): F[fs2.Stream[F, RenderedTemplate]] = {
        for {
          logger      <- Slf4jLogger.create[F]
          _           <- logger.info(s"Reading schema from file ${conf.input.schema.getAbsolutePath()}")
          schema      <- SchemaReader.make[F]().read(conf.input.schema)
          sources     <- TemplateReader.make[F]().read(conf.input.template, decodeNewLineAsTemplate = false)
          templateGen <- makeGenerator(schema, sources)
        } yield GeneratorStream.stream[F](args.numberOfSamplesToGenerate, templateGen)
      }

      private def makeGenerator(schema: GeneratorsSchema, sources: List[SourceTemplate]) = Async[F].delay {
        TemplateGenerator.make(sources, schema.generators, Nil)
      }
    }
  }

}
