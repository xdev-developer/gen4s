package io.gen4s.conf

import java.net.URI

import scala.deriving.Mirror
import scala.util.Try

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
import pureconfig.error.CannotConvert
import pureconfig.error.FailureReason
import pureconfig.generic.*
import pureconfig.generic.semiauto.deriveReader
import pureconfig.module.enumeratum.*
import software.amazon.awssdk.endpoints.Endpoint
import software.amazon.awssdk.regions.Region

given ConfigReader[Topic] = ConfigReader.fromString { value =>
  Topic(value).asRight[FailureReason]
}

given ConfigReader[BootstrapServers] = ConfigReader.fromString { value =>
  BootstrapServers(value).asRight[FailureReason]
}

given ConfigReader[KafkaProducerConfig] = ConfigReader.derived[KafkaProducerConfig]

given ConfigReader[Region] = ConfigReader.fromString { value =>
  Region.of(value).asRight[FailureReason]
}

given ConfigReader[Endpoint] = ConfigReader.fromString { value =>
  Try(new URI(value)).toEither
    .map(u => Endpoint.builder().url(u).build)
    .leftMap(e => CannotConvert(value, "Endpoint", e.getMessage))
}

object OutputConfig {
  given ConfigReader[OutputConfig] = deriveReader[OutputConfig]
}

final case class OutputConfig(
  writer: Output,
  transformers: Set[OutputTransformer] = Set.empty[OutputTransformer],
  validators: Set[OutputValidator] = Set.empty[OutputValidator])
