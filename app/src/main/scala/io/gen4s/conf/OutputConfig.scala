package io.gen4s.conf

import cats.implicits.*
import io.gen4s.core.templating.{OutputTransformer, OutputValidator}
import io.gen4s.core.Domain.BootstrapServers
import io.gen4s.core.Domain.Topic
import io.gen4s.outputs.KafkaProducerConfig
import io.gen4s.outputs.Output

import eu.timepit.refined.pureconfig.*
import eu.timepit.refined.types.numeric.PosInt
import eu.timepit.refined.types.string.NonEmptyString
import pureconfig.*
import pureconfig.error.FailureReason
import pureconfig.generic.derivation.default.*
import pureconfig.module.enumeratum.*

given ConfigReader[Topic] = ConfigReader.fromString { value =>
  Topic(value).asRight[FailureReason]
}

given ConfigReader[BootstrapServers] = ConfigReader.fromString { value =>
  BootstrapServers(value).asRight[FailureReason]
}

given ConfigReader[KafkaProducerConfig] = ConfigReader.derived[KafkaProducerConfig]

final case class OutputConfig(
  writer: Output,
  transformers: Set[OutputTransformer] = Set.empty[OutputTransformer],
  validators: Set[OutputValidator] = Set.empty[OutputValidator])
    derives ConfigReader
