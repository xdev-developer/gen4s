package io.gen4s.test

import java.io.File

import org.scalatest.freespec.AsyncFreeSpec
import org.scalatest.matchers.should.Matchers
import org.scalatest.OptionValues

import cats.effect.testing.scalatest.AsyncIOSpec
import cats.effect.IO
import cats.syntax.option.*
import io.gen4s.core.generators.*
import io.gen4s.RecordsReader

class RecordsReaderTest extends AsyncFreeSpec with AsyncIOSpec with Matchers with OptionValues {

  "Records reader" - {
    "Read csv file" in {
      RecordsReader
        .make[IO]()
        .read(new File("./app/src/test/resources/entities.csv"))
        .asserting { list =>
          list should not be empty
          list.headOption.map(_.fields) shouldBe Map(
            Variable("id")   -> GeneratedValue.fromString("1"),
            Variable("user") -> GeneratedValue.fromString("Denys")
          ).some
        }
    }
  }
}
