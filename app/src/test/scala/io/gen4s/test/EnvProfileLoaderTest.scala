package io.gen4s.test

import java.io.File

import org.scalatest.OptionValues
import org.scalatest.freespec.AsyncFreeSpec
import org.scalatest.matchers.should.Matchers

import cats.effect.IO
import cats.effect.testing.scalatest.AsyncIOSpec
import io.gen4s.cli.EnvironmentVariablesProfileLoader

class EnvProfileLoaderTest extends AsyncFreeSpec with AsyncIOSpec with Matchers with OptionValues {

  "Env profile loader" - {
    "Load env profile" in
      EnvironmentVariablesProfileLoader
        .make[IO]()
        .fromFile(new File("./app/src/test/resources/sample.profile"))
        .asserting { p =>
          p.vars shouldBe Map(
            "A" -> "1",
            "B" -> "Test",
            "C" -> "Text with space"
          )
        }
  }
}
