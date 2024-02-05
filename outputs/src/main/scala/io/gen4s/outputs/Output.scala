package io.gen4s.outputs

import java.io.File
import java.nio.file.{Path, Paths}

import org.apache.commons.io.FilenameUtils

import io.gen4s.core.Domain.*

import enumeratum.EnumEntry
import eu.timepit.refined.types.numeric.PosInt
import eu.timepit.refined.types.string.NonEmptyString

sealed trait Output {
  def description(): String
}

case class StdOutput() extends Output {
  override def description(): String = "std-output"
}

trait KafkaOutputBase {
  val topic: Topic
  val headers: Map[String, String]
  val batchSize: PosInt
  val decodeInputAsKeyValue: Boolean
}

final case class KafkaOutput(
  topic: Topic,
  bootstrapServers: BootstrapServers,
  decodeInputAsKeyValue: Boolean = false,
  headers: Map[String, String] = Map.empty,
  batchSize: PosInt = PosInt.unsafeFrom(1000),
  producerConfig: Option[KafkaProducerConfig] = None)
    extends Output
    with KafkaOutputBase {

  def kafkaProducerConfig: KafkaProducerConfig = producerConfig.getOrElse(KafkaProducerConfig.default)

  override def description(): String = s"Kafka output: topic: $topic, bootstrap-servers: $bootstrapServers"
}

final case class AvroConfig(
  schemaRegistryUrl: String,
  keySchema: Option[File] = None,
  valueSchema: Option[File] = None,
  autoRegisterSchemas: Boolean = false,
  registryClientMaxCacheSize: Int = 1000
)

final case class KafkaAvroOutput(
  topic: Topic,
  bootstrapServers: BootstrapServers,
  avroConfig: AvroConfig,
  decodeInputAsKeyValue: Boolean = false,
  headers: Map[String, String] = Map.empty,
  batchSize: PosInt = PosInt.unsafeFrom(1000),
  producerConfig: Option[KafkaProducerConfig] = None)
    extends Output
    with KafkaOutputBase {

  def kafkaProducerConfig: KafkaProducerConfig = producerConfig.getOrElse(KafkaProducerConfig.default)

  override def description(): String = s"Kafka avro output: topic: $topic, bootstrap-servers: $bootstrapServers"
}

final case class ProtobufDescriptorConfig(
  file: File,
  messageType: String
)

final case class ProtobufConfig(
  schemaRegistryUrl: String,
  valueDescriptor: ProtobufDescriptorConfig,
  autoRegisterSchemas: Boolean = false,
  registryClientMaxCacheSize: Int = 1000
)

final case class KafkaProtobufOutput(
  topic: Topic,
  bootstrapServers: BootstrapServers,
  protoConfig: ProtobufConfig,
  decodeInputAsKeyValue: Boolean = false,
  headers: Map[String, String] = Map.empty,
  batchSize: PosInt = PosInt.unsafeFrom(1000),
  producerConfig: Option[KafkaProducerConfig] = None)
    extends Output
    with KafkaOutputBase {

  def kafkaProducerConfig: KafkaProducerConfig = producerConfig.getOrElse(KafkaProducerConfig.default)

  override def description(): String = s"Kafka proto-buff output: topic: $topic, bootstrap-servers: $bootstrapServers"
}

sealed abstract class HttpMethods(override val entryName: String) extends EnumEntry

object HttpMethods extends enumeratum.Enum[HttpMethods] {
  val values: IndexedSeq[HttpMethods] = findValues

  case object Post extends HttpMethods("POST")
  case object Put  extends HttpMethods("PUT")
}

sealed abstract class HttpContentTypes(override val entryName: String) extends EnumEntry

object HttpContentTypes extends enumeratum.Enum[HttpContentTypes] {
  val values: IndexedSeq[HttpContentTypes] = findValues

  case object ApplicationJson extends HttpContentTypes("application/json")
  case object TextPlain       extends HttpContentTypes("text/plain")
  case object TextCsv         extends HttpContentTypes("text/csv")
  case object TextXml         extends HttpContentTypes("text/xml")
}

final case class HttpOutput(
  url: String,
  method: HttpMethods,
  parallelism: PosInt = PosInt.unsafeFrom(1),
  headers: Map[String, String] = Map.empty,
  contentType: HttpContentTypes = HttpContentTypes.TextPlain,
  stopOnError: Boolean = true
) extends Output {
  override def description(): String = s"Http output: url: $url, method: $method"
}

final case class FsOutput(dir: NonEmptyString, filenamePattern: NonEmptyString) extends Output {

  def path(): Path =
    Paths.get(dir.value, FilenameUtils.getName(filenamePattern.value.format(System.currentTimeMillis())))

  override def description(): String = s"File System output: path: ${path()}"
}
