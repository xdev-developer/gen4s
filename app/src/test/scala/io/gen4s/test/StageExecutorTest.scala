package io.gen4s.test

import org.scalatest.funspec.AsyncFunSpec
import org.scalatest.matchers.should.Matchers
import org.typelevel.log4cats.SelfAwareStructuredLogger
import org.typelevel.log4cats.slf4j.Slf4jLogger

import cats.effect.testing.scalatest.AsyncIOSpec
import cats.effect.{IO, Sync}
import io.gen4s.cli.Args
import io.gen4s.conf.{InputConfig, OutputConfig, StageConfig}
import io.gen4s.core.Domain.NumberOfSamplesToGenerate
import io.gen4s.outputs.StdOutput
import io.gen4s.stage.StageExecutor

class StageExecutorTest extends AsyncFunSpec with AsyncIOSpec with Matchers {

  private val numSamples = NumberOfSamplesToGenerate(1)

  given unsafeLogger[F[_]: Sync]: SelfAwareStructuredLogger[F] = Slf4jLogger.getLogger[F]

  private val executor = StageExecutor.make[IO](
    name = "Playground",
    args = Args(numberOfSamplesToGenerate = numSamples),
    conf = StageConfig(
      InputConfig(
        schema = new java.io.File("./examples/playground/input.schema.json"),
        template = new java.io.File("./examples/playground/input.template.json")
      ),
      OutputConfig(StdOutput())
    )
  )

  describe("Generator Stream") {

    it("Run stage stream") {
      executor.flatMap(_.exec()).assertNoException
    }

    it("Preview stage stream") {
      executor.flatMap(_.preview()).assertNoException
    }

  }
}
