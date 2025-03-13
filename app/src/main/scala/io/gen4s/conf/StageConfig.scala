package io.gen4s.conf

import java.io.File

import io.gen4s.core.generators.{GeneratedValue, Variable}
import io.gen4s.core.Domain.NumberOfSamplesToGenerate
import io.gen4s.core.InputRecord

import scala.concurrent.duration.FiniteDuration

import pureconfig.*

final case class StageConfig(input: InputConfig, output: OutputConfig) derives ConfigReader

final case class StageInput(
  name: Option[String] = None,
  samples: NumberOfSamplesToGenerate = NumberOfSamplesToGenerate(1),
  configFile: File,
  delay: Option[FiniteDuration],
  overrides: Option[Map[String, Option[String]]])
    derives ConfigReader {

  def overridesInput: Option[InputRecord] = {
    overrides.map { o =>
      InputRecord(o.map { case (k, v) =>
        Variable(k) -> v.map(GeneratedValue.fromString).getOrElse(GeneratedValue.nullValue())
      })
    }
  }
}
