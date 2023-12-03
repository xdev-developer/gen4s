package io.gen4s

import java.io.File

import com.github.tototoshi.csv.CSVReader

import cats.effect.kernel.{Resource, Sync}
import io.circe.Json
import io.gen4s.core.generators.{GeneratedValue, Variable}
import io.gen4s.core.InputRecord
import io.gen4s.generators.impl.StaticValueGenerator

trait RecordsReader[F[_]] {
  def read(file: File): F[List[InputRecord]]
}

object RecordsReader {

  def make[F[_]: Sync](): RecordsReader[F] = new RecordsReader[F] {

    private val noneV = Variable("_")

    override def read(file: File): F[List[InputRecord]] = {
      import com.github.tototoshi.csv.defaultCSVFormat
      val F = Sync[F]

      Resource
        .make(F.blocking(CSVReader.open(file)))(r => F.blocking(r.close()))
        .use { reader =>
          F.blocking {
            reader
              .allWithHeaders()
              .map[InputRecord] { fields =>
                val recordFields: Map[Variable, GeneratedValue] = fields.map { case (k, v) =>
                  Variable(k) -> StaticValueGenerator(noneV, Json.fromString(v)).gen()
                }

                InputRecord(recordFields)
              }
          }
        }
    }
  }
}
