package io.gen4s.core

import java.io.File

import cats.effect.kernel.Sync
import cats.implicits.*
import io.circe.parser.decode

trait SchemaReader[F[_]] {
  def read(content: String): F[GeneratorsSchema]
  def read(file: File): F[GeneratorsSchema]
}

object SchemaReader {

  def make[F[_]: Sync](): SchemaReader[F] = new SchemaReader[F] {

    override def read(file: File): F[GeneratorsSchema] = {
      FileUtils.readFile(file).use(c => read(c))
    }

    override def read(content: String): F[GeneratorsSchema] = {
      decode[GeneratorsSchema](content).liftTo[F]
    }

  }
}
