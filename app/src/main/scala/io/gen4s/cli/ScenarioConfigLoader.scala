package io.gen4s.cli

import java.io.File

import cats.effect.Sync
import io.gen4s.conf.*

trait ScenarioConfigLoader[F[_]] {
  def withEnvProfile(p: EnvProfileConfig): F[ScenarioConfig]
  def withEmptyEnvProfile(): F[ScenarioConfig]
}

object ScenarioConfigLoader {

  def fromFile[F[_]: Sync](file: File): ScenarioConfigLoader[F] = new ScenarioConfigLoader[F] {

    override def withEnvProfile(profileConf: EnvProfileConfig): F[ScenarioConfig] = {
      import pureconfig.*
      import pureconfig.module.catseffect.syntax.*

      ConfigSource
        .file(file)
        .withFallback(ConfigSource.fromConfig(profileConf.value))
        .loadF[F, ScenarioConfig]()
    }

    override def withEmptyEnvProfile(): F[ScenarioConfig] = withEnvProfile(EnvProfileConfig.empty)
  }
}
