package io.gen4s.conf

import com.typesafe.config.Config
import com.typesafe.config.ConfigFactory

import scala.util.Try

import cats.implicits.*
import io.gen4s.core.outputs.Output
import io.gen4s.core.Domain.BootstrapServers
import io.gen4s.core.Domain.Topic

import enumeratum.EnumEntry
import eu.timepit.refined.pureconfig.*
import eu.timepit.refined.types.numeric.PosInt
import eu.timepit.refined.types.string.NonEmptyString
import pureconfig.*
import pureconfig.generic.derivation.default.*
import pureconfig.module.enumeratum.*

given ConfigReader[Topic] = ConfigReader.fromString { value =>
  Topic(value).asRight
}

given ConfigReader[BootstrapServers] = ConfigReader.fromString { value =>
  BootstrapServers(value).asRight
}

case class OutputConfig(writer: Output) derives ConfigReader
