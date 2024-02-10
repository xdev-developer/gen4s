package io.gen4s.generators.test

import org.scalatest.funspec.AsyncFunSpec
import org.scalatest.matchers.should.Matchers

import cats.effect.testing.scalatest.AsyncIOSpec
import cats.effect.IO
import io.gen4s.core.generators.Variable
import io.gen4s.generators.{Generators, SchemaReader}
import io.gen4s.generators.impl.TimestampGenerator

class SchemaReaderTest extends AsyncFunSpec with AsyncIOSpec with Matchers {

  describe("Schema reader") {

    it("Read empty schema from string") {
      val reader = SchemaReader.make[IO]()
      reader.read("""{"generators": []}""").asserting { r =>
        r.generators should contain theSameElementsAs List.empty[Generators]
      }
    }

    it("Read schema from string") {
      val reader = SchemaReader.make[IO]()
      reader
        .read("""{
          "generators": [
            {"variable": "ts", "type": "timestamp"}
          ]
        }""")
        .asserting { r =>
          r.generators should contain theSameElementsAs List(
            TimestampGenerator(variable = Variable("ts"))
          )
        }
    }
  }
}
