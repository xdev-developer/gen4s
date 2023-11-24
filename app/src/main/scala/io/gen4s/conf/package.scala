package io.gen4s

import com.typesafe.config.{Config, ConfigFactory}

package object conf {

  case class EnvProfileConfig(value: Config) extends AnyVal

  object EnvProfileConfig {
    def empty: EnvProfileConfig = EnvProfileConfig(ConfigFactory.empty())
  }

}
