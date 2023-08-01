package io.gen4s.cli

import java.io.File

import com.typesafe.config.Config

import cats.effect.Sync
import io.gen4s.conf.*

trait StageConfigLoader[F[_]] {
  def withEnvProfile(p: EnvProfileConfig): F[StageConfig]
  def withEmptyEnvProfile(): F[StageConfig]
}

object StageConfigLoader {

  def fromFile[F[_]: Sync](file: File): StageConfigLoader[F] = new StageConfigLoader[F] {

    override def withEnvProfile(profileConf: EnvProfileConfig): F[StageConfig] = {
      import pureconfig.*
      import pureconfig.generic.derivation.default.*
      import pureconfig.ConfigSource
      import pureconfig.module.catseffect.syntax.*

      ConfigSource
        .file(file)
        .withFallback(ConfigSource.fromConfig(profileConf.value))
        .loadF[F, StageConfig]()
    }

    override def withEmptyEnvProfile(): F[StageConfig] = withEnvProfile(EnvProfileConfig.empty)
  }

}
