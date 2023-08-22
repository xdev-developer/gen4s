package io.gen4s.core

import java.io.File

import scala.io.Source

import cats.effect.kernel.Resource
import cats.effect.kernel.Sync

object FileUtils {

  def readFile[F[_]: Sync](in: File): Resource[F, String] = {
    Resource.fromAutoCloseable(Sync[F].blocking(Source.fromFile(in))).map(_.mkString)
  }

  def readLines[F[_]: Sync](in: File): Resource[F, Iterator[String]] = {
    Resource.fromAutoCloseable(Sync[F].blocking(Source.fromFile(in))).map(_.getLines())
  }

}
