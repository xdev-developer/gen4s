package io.gen4s

import java.io.File

import cats.data.NonEmptyList
import cats.effect.kernel.Sync
import cats.implicits.*
import cats.FunctorFilter
import io.gen4s.core.templating.SourceTemplate
import io.gen4s.core.FileUtils

trait TemplateReader[F[_]] {
  def read(file: File, decodeNewLineAsTemplate: Boolean): F[NonEmptyList[SourceTemplate]]
}

object TemplateReader {

  private def templateContentFilter[F[_]: FunctorFilter](f: F[SourceTemplate]): F[SourceTemplate] = {
    import cats.syntax.functorFilter.*
    f.filter(_.content.trim.nonEmpty)
      .filterNot(_.content.trim.startsWith("#"))
      .filterNot(_.content.trim.startsWith("//"))
  }

  def make[F[_]: Sync](): TemplateReader[F] = new TemplateReader[F] {

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
