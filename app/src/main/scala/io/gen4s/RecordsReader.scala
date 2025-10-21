package io.gen4s

import java.io.File

import cats.effect.kernel.{Resource, Sync}
import io.gen4s.core.InputRecord
import io.gen4s.core.generators.{GeneratedValue, Variable}

/**
 * A trait that defines the method for reading records from a file.
 *
 * @tparam F the effect type, which must have a Sync type class instance
 */
trait RecordsReader[F[_]] {
  def read(file: File): F[List[InputRecord]]
}

object RecordsReader {

  /**
   * Factory method for creating an instance of RecordsReader.
   *
   * @tparam F the effect type, which must have a Sync type class instance
   * @return an instance of RecordsReader
   */
  def make[F[_]: Sync](): RecordsReader[F] = new RecordsReader[F] {

    /**
     * Reads records from a CSV file and converts them to a list of InputRecord.
     *
     * @param file the CSV file to read from
     * @return a list of InputRecord
     */
    override def read(file: File): F[List[InputRecord]] = {
      import com.github.tototoshi.csv._
      val F = Sync[F]

      Resource
        .make(F.blocking(CSVReader.open(file)))(r => F.blocking(r.close()))
        .use { reader =>
          F.blocking {
            reader
              .allWithHeaders()
              .map[InputRecord] { fields =>
                val recordFields: Map[Variable, GeneratedValue] = fields.map { case (k, v) =>
                  Variable(k) -> GeneratedValue.fromString(v)
                }

                InputRecord(recordFields)
              }
          }
        }
    }
  }
}
