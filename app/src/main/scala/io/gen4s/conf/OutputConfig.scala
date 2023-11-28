package io.gen4s.conf

import cats.implicits.*
import io.gen4s.core.templating.OutputTransformer
import io.gen4s.core.Domain.BootstrapServers
import io.gen4s.core.Domain.Topic
import io.gen4s.outputs.KafkaProducerConfig
import io.gen4s.outputs.Output

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

given ConfigReader[KafkaProducerConfig] = ConfigReader.derived[KafkaProducerConfig]

case class OutputConfig(writer: Output, transformers: Set[OutputTransformer] = Set.empty[OutputTransformer])
    derives ConfigReader
