package io.gen4s.conf

import cats.data.NonEmptyList

import pureconfig.*
import pureconfig.generic.derivation.default.*
import pureconfig.module.cats.*

final case class ScenarioConfig(stages: NonEmptyList[StageInput]) derives ConfigReader
