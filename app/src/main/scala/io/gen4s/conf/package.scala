package io.gen4s

import com.typesafe.config.{Config, ConfigFactory}

package object conf {

  opaque type EnvProfileConfig = Config

  object EnvProfileConfig {
    def apply(config: Config): EnvProfileConfig       = config
    extension (p: EnvProfileConfig) def value: Config = p

    def empty: EnvProfileConfig = EnvProfileConfig(ConfigFactory.empty())
  }

}
