package io.gen4s.test

import org.scalatest.freespec.AsyncFreeSpec
import org.scalatest.matchers.should.Matchers
import org.scalatest.OptionValues

import cats.effect.{ExitCode, IO}
import cats.effect.testing.scalatest.AsyncIOSpec
import io.gen4s.App

class AppTest extends AsyncFreeSpec with AsyncIOSpec with Matchers with OptionValues {

  "App" - {
    "Run" in {
      App.run(List("run", "-c", "./examples/playground/config.conf")).asserting { code =>
        code shouldBe ExitCode.Success
      }
    }

    "Preview" in {
      App.run(List("preview", "-c", "./examples/playground/config.conf")).asserting { code =>
        code shouldBe ExitCode.Success
      }
    }

    "Run scenario" in {
      App.run(List("scenario", "-c", "./examples/scenario/scenario.conf")).asserting { code =>
        code shouldBe ExitCode.Success
      }
    }
  }
}
