package io.gen4s.test

import java.util.concurrent.TimeUnit

import org.scalatest.freespec.AsyncFreeSpec
import org.scalatest.matchers.should.Matchers
import org.scalatest.OptionValues

import cats.effect.kernel.Sync
import cats.effect.testing.scalatest.AsyncIOSpec
import cats.effect.IO
import io.gen4s.conf.*

import scala.concurrent.duration.FiniteDuration

class ScenarioLoaderTest extends AsyncFreeSpec with AsyncIOSpec with Matchers with OptionValues {

  private def load[F[_]: Sync](str: String): F[ScenarioConfig] = {
    import pureconfig.*
    import pureconfig.module.catseffect.syntax.*

    ConfigSource
      .string(str)
      .loadF[F, ScenarioConfig]()
  }

  "Scenario config loader" - {

    "Load std output" in {
      load[IO]("""stages: [
                 | { name: "My cool stage", samples: 1, config-file: "/tmp/file", delay: 5 seconds }
                 |]""".stripMargin)
        .asserting { out =>
          val stages = out.stages.toList
          stages should not be empty
          stages.headOption.flatMap(_.name).value shouldBe "My cool stage"
          stages.headOption.map(_.configFile.getPath).value shouldBe "/tmp/file"
          stages.headOption.flatMap(_.delay).value shouldBe FiniteDuration(5, TimeUnit.SECONDS)
        }
    }
  }

}
