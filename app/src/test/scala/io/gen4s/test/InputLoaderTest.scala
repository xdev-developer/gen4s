package io.gen4s.test

import org.scalatest.freespec.AsyncFreeSpec
import org.scalatest.matchers.should.Matchers

import cats.effect.IO
import cats.effect.kernel.Sync
import cats.effect.testing.scalatest.AsyncIOSpec
import io.gen4s.conf.*

class InputLoaderTest extends AsyncFreeSpec with AsyncIOSpec with Matchers {

  private def load[F[_]: Sync](str: String): F[InputConfig] = {
    import pureconfig.*
    import pureconfig.module.catseffect.syntax.*

    ConfigSource
      .string(str)
      .loadF[F, InputConfig]()
  }

  "Input loader" - {

    "Load input" in
      load[IO]("""{
                 |    schema = "input.schema.json"
                 |    template = "input.template.json"
                 |    decode-new-line-as-template = false
                 |    csv-records = "entities.csv"
                 |    global-variables = ["name", "timestamp"]
                 |}""".stripMargin).assertNoException
  }
}
