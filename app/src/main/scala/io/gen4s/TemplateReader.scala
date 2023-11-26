package io.gen4s

import java.io.File

import cats.effect.kernel.Sync
import cats.implicits.*
import cats.FunctorFilter
import io.gen4s.core.templating.SourceTemplate
import io.gen4s.core.FileUtils

trait TemplateReader[F[_]] {
  def read(file: File, decodeNewLineAsTemplate: Boolean): F[List[SourceTemplate]]
}

object TemplateReader {

  private def templateContentFilter[F[_]: FunctorFilter](f: F[SourceTemplate]): F[SourceTemplate] = {
    import cats.syntax.functorFilter.*
    f.filter(_.content.trim.nonEmpty)
      .filterNot(_.content.trim.startsWith("#"))
      .filterNot(_.content.trim.startsWith("//"))
  }

  def make[F[_]: Sync](): TemplateReader[F] = new TemplateReader[F] {

    override def read(file: File, decodeNewLineAsTemplate: Boolean): F[List[SourceTemplate]] = {
      if (decodeNewLineAsTemplate) {
        FileUtils.readLines(file).use { lines =>
          templateContentFilter[List](lines.map(c => SourceTemplate(c)).toList).pure[F]
        }

      } else {
        FileUtils.readFile(file).use(c => List(SourceTemplate(c)).pure[F])
      }

    }

  }
}
