package io.gen4s.conf

import java.io.File

import io.gen4s.core.Domain.NumberOfSamplesToGenerate

import scala.concurrent.duration.FiniteDuration

import pureconfig.*
import pureconfig.generic.derivation.default.*

final case class StageConfig(input: InputConfig, output: OutputConfig) derives ConfigReader

final case class StageInput(
  name: Option[String] = None,
  samples: NumberOfSamplesToGenerate = NumberOfSamplesToGenerate(1),
  configFile: File,
  delay: Option[FiniteDuration])
    derives ConfigReader
