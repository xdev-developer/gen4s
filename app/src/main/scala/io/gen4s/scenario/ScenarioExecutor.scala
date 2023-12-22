package io.gen4s.scenario

import org.typelevel.log4cats.Logger

import cats.effect.kernel.Async
import cats.effect.std.Console as EffConsole
import cats.implicits.*
import io.gen4s.cli.{Args, StageConfigLoader}
import io.gen4s.conf.{EnvProfileConfig, ScenarioConfig, StageInput}
import io.gen4s.stage.StageExecutor

import scala.concurrent.duration.FiniteDuration

import fs2.io.file.Files

trait ScenarioExecutor[F[_]] {
  def exec(): F[Unit]
}

object ScenarioExecutor {

  def make[F[_]: Async: EffConsole: Files: Logger](
    args: Args,
    conf: ScenarioConfig,
    envProfileConfig: EnvProfileConfig): F[ScenarioExecutor[F]] = Async[F].delay {
    new ScenarioExecutor[F]() {
      override def exec(): F[Unit] = {
        initStages(conf).flatMap { stages =>
          stages.traverse { case (in, executor) =>
            executor.exec() *> (in.delay match {
              case Some(delay) => Logger[F].info(s"Waiting $delay ...") *> Async[F].sleep(delay)
              case None        => Async[F].unit
            })
          }
        }.void
      }

      private def initStages(cfg: ScenarioConfig): F[List[(StageInput, StageExecutor[F])]] = {
        cfg.stages
          .map { sc =>
            val name = sc.name.getOrElse(sc.configFile.getAbsolutePath)
            val args = Args(numberOfSamplesToGenerate = sc.samples, stageDelay = sc.delay)

            StageConfigLoader
              .fromFile[F](sc.configFile)
              .withEnvProfile(envProfileConfig)
              .flatMap { conf =>
                StageExecutor
                  .make[F](name, args, conf)
                  .map(e => sc -> e)
              }
          }
          .toList
          .sequence
      }
    }
  }
}
