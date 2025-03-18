package io.gen4s.conf

import cats.data.NonEmptyList

import pureconfig.*
import pureconfig.generic.*
import pureconfig.generic.semiauto.deriveReader
import pureconfig.module.cats.*

object ScenarioConfig {
  given ConfigReader[ScenarioConfig] = deriveReader[ScenarioConfig]
}

final case class ScenarioConfig(stages: NonEmptyList[StageInput])
