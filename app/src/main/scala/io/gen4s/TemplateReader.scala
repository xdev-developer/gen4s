package io.gen4s

import java.io.File

import cats.FunctorFilter
import cats.data.NonEmptyList
import cats.effect.kernel.Sync
import cats.implicits.*
import io.gen4s.core.FileUtils
import io.gen4s.core.templating.SourceTemplate

trait TemplateReader[F[_]] {
  def read(file: File, decodeNewLineAsTemplate: Boolean): F[NonEmptyList[SourceTemplate]]
}

object TemplateReader {

  private def templateContentFilter[F[_]: FunctorFilter](f: F[SourceTemplate]): F[SourceTemplate] = {
    import cats.syntax.functorFilter.*
    f.filter(_.value.trim.nonEmpty)
      .filterNot(_.value.trim.startsWith("#"))
      .filterNot(_.value.trim.startsWith("-- "))
      .filterNot(_.value.trim.startsWith("//"))
  }

  def make[F[_]: Sync](): TemplateReader[F] = new TemplateReader[F] {

    /**
     * Reads templates from a file and converts them to a list of SourceTemplate.
     * If decodeNewLineAsTemplate is true, each line is considered a separate template.
     * Otherwise, the entire file content is considered a single template.
     *
     * @param file                    the file to read from
     * @param decodeNewLineAsTemplate a flag indicating whether each line should be considered a separate template
     * @return a list of SourceTemplate
     */
    override def read(file: File, decodeNewLineAsTemplate: Boolean): F[NonEmptyList[SourceTemplate]] = {
      if (decodeNewLineAsTemplate) {
        FileUtils.readLines(file).use { lines =>
          val list = templateContentFilter[List](lines.map(c => SourceTemplate(c)).toList)
          NonEmptyList.fromList(list) match {
            case Some(v) => v.pure[F]
            case None    => Sync[F].raiseError(new Exception("Template source cannot be empty."))
          }
        }

      } else {
        FileUtils.readFile(file).use(c => NonEmptyList.one(SourceTemplate(c)).pure[F])
      }

    }

  }
}
