package io.gen4s

import com.typesafe.config.{Config, ConfigFactory}

import io.gen4s.core.generators.Variable
import io.gen4s.core.Domain.NumberOfSamplesToGenerate

import pureconfig.ConfigReader

package object conf {

  opaque type EnvProfileConfig = Config

  object EnvProfileConfig {
    def apply(config: Config): EnvProfileConfig       = config
    extension (p: EnvProfileConfig) def value: Config = p

    def empty: EnvProfileConfig = EnvProfileConfig(ConfigFactory.empty())
  }

  given ConfigReader[NumberOfSamplesToGenerate] = ConfigReader[Int].map(n => NumberOfSamplesToGenerate(n))
  given ConfigReader[Variable]                  = ConfigReader[String].map(v => Variable(v))

}
