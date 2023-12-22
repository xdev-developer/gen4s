package io.gen4s.test

import org.scalatest.funspec.AsyncFunSpec
import org.scalatest.matchers.should.Matchers
import org.typelevel.log4cats.slf4j.Slf4jLogger
import org.typelevel.log4cats.SelfAwareStructuredLogger

import com.typesafe.config.ConfigFactory

import cats.data.NonEmptyList
import cats.effect.{IO, Sync}
import cats.effect.testing.scalatest.AsyncIOSpec
import io.gen4s.cli.Args
import io.gen4s.conf.{EnvProfileConfig, ScenarioConfig, StageInput}
import io.gen4s.core.Domain.NumberOfSamplesToGenerate
import io.gen4s.scenario.ScenarioExecutor

import scala.concurrent.duration.*

class ScenarioExecutorTest extends AsyncFunSpec with AsyncIOSpec with Matchers {

  private val numSamples = NumberOfSamplesToGenerate(5)

  implicit def unsafeLogger[F[_]: Sync]: SelfAwareStructuredLogger[F] = Slf4jLogger.getLogger[F]

  describe("Generator Stream") {

    it("Run scenario generation stream") {
      val executor = ScenarioExecutor.make[IO](
        args = Args(numberOfSamplesToGenerate = numSamples),
        envProfileConfig = EnvProfileConfig(ConfigFactory.empty()),
        conf = ScenarioConfig(
          NonEmptyList.one(
            StageInput(
              name = Some("Playground Stage"),
              samples = numSamples,
              configFile = new java.io.File("./examples/playground/config.conf"),
              delay = Some(1.second)
            )
          )
        )
      )

      executor.flatMap(_.exec()).assertNoException
    }
  }
}
