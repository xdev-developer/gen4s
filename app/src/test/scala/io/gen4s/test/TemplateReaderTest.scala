package io.gen4s.test

import java.io.File

import org.scalatest.funspec.AsyncFunSpec
import org.scalatest.matchers.should.Matchers

import cats.effect.testing.scalatest.AsyncIOSpec
import cats.effect.IO
import io.gen4s.TemplateReader

class TemplateReaderTest extends AsyncFunSpec with AsyncIOSpec with Matchers {

  describe("Template reader") {

    it("Read json template") {
      TemplateReader
        .make[IO]()
        .read(new File("./app/src/test/resources/single.json"), decodeNewLineAsTemplate = false)
        .asserting { list =>
          list.toList should not be empty
          list.head.content shouldBe "{\"hello\":\"world\"}\n"
        }
    }

    it("Read multiple templates") {
      TemplateReader
        .make[IO]()
        .read(new File("./app/src/test/resources/multi.template"), decodeNewLineAsTemplate = true)
        .asserting { list =>
          list.toList should not be empty
          list.toList.map(_.content) shouldBe List(
            """{"name": "first"}""",
            """{"name": "second"}""",
            """{"name": "third"}"""
          )
        }
    }
  }
}
