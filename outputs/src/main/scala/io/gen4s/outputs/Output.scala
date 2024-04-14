package io.gen4s.outputs

import java.io.File
import java.nio.file.{Path, Paths}

import org.apache.commons.io.FilenameUtils

import io.gen4s.core.Domain.*

import enumeratum.EnumEntry
import eu.timepit.refined.types.numeric.PosInt
import eu.timepit.refined.types.string.NonEmptyString
import fs2.aws.s3.models.Models.PartSizeMB
import software.amazon.awssdk.endpoints.Endpoint
import software.amazon.awssdk.regions.Region

sealed trait Output {
  def description(): String
}

/**
 * Represents the standard output.
 */
final case class StdOutput() extends Output {
  override def description(): String = "std-output"
}

/**
 * Base trait for Kafka outputs.
 */
trait KafkaOutputBase {
  val topic: Topic
  val headers: Map[String, String]
  val batchSize: PosInt
  val decodeInputAsKeyValue: Boolean
}

/**
 * Represents a Kafka output.
 *
 * @param topic                 the topic to which the data will be published.
 * @param bootstrapServers      the bootstrap servers for the Kafka cluster.
 * @param decodeInputAsKeyValue whether to decode the input as key-value pairs.
 * @param headers               the headers to be included in the Kafka message.
 * @param batchSize             the batch size for publishing messages.
 * @param producerConfig        the configuration for the Kafka producer.
 */
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

/**
 * Represents the configuration for Avro serialization in Kafka.
 *
 * @param schemaRegistryUrl          The URL of the schema registry. This is where Avro schemas are stored and retrieved.
 * @param keySchema                  An optional file that contains the Avro schema for the key of the Kafka message. If not provided, the key is assumed to be a string.
 * @param valueSchema                An optional file that contains the Avro schema for the value of the Kafka message. If not provided, the value is assumed to be a string.
 * @param autoRegisterSchemas        A boolean flag that indicates whether to automatically register new schemas with the schema registry. If set to false, all schemas must be pre-registered.
 * @param registryClientMaxCacheSize The maximum number of schemas that the client will cache from the schema registry. This can help improve performance by reducing the number of requests to the schema registry.
 */
final case class AvroConfig(
  schemaRegistryUrl: String,
  keySchema: Option[File] = None,
  valueSchema: Option[File] = None,
  autoRegisterSchemas: Boolean = false,
  registryClientMaxCacheSize: Int = 1000
)

/**
 * Represents a Kafka output with Avro serialization.
 *
 * @param topic                 the topic to which the data will be published.
 * @param bootstrapServers      the bootstrap servers for the Kafka cluster.
 * @param avroConfig            the configuration for Avro serialization.
 * @param decodeInputAsKeyValue whether to decode the input as key-value pairs.
 * @param headers               the headers to be included in the Kafka message.
 * @param batchSize             the batch size for publishing messages.
 * @param producerConfig        the configuration for the Kafka producer.
 */
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

  /**
   * Provides the Kafka producer configuration.
   * If no configuration is provided, it defaults to the KafkaProducerConfig.default.
   *
   * @return the Kafka producer configuration.
   */
  def kafkaProducerConfig: KafkaProducerConfig = producerConfig.getOrElse(KafkaProducerConfig.default)

  override def description(): String = s"Kafka avro output: topic: $topic, bootstrap-servers: $bootstrapServers"
}

/**
 * Represents the configuration for a Protobuf descriptor.
 *
 * @param file        The file that contains the Protobuf descriptor.
 * @param messageType The type of the message in the Protobuf descriptor (for ex. Person).
 */
final case class ProtobufDescriptorConfig(
  file: File,
  messageType: String
)

/**
 * Represents the configuration for Protobuf serialization in Kafka.
 *
 * @param schemaRegistryUrl          The URL of the schema registry. This is where Protobuf schemas are stored and retrieved.
 * @param valueDescriptor            The configuration for the Protobuf descriptor.
 * @param autoRegisterSchemas        A boolean flag that indicates whether to automatically register new schemas with the schema registry. If set to false, all schemas must be pre-registered.
 * @param registryClientMaxCacheSize The maximum number of schemas that the client will cache from the schema registry. This can help improve performance by reducing the number of requests to the schema registry.
 */
final case class ProtobufConfig(
  schemaRegistryUrl: String,
  valueDescriptor: ProtobufDescriptorConfig,
  autoRegisterSchemas: Boolean = false,
  registryClientMaxCacheSize: Int = 1000
)

/**
 * Represents a Kafka output with Protobuf serialization.
 *
 * @param topic                 the topic to which the data will be published.
 * @param bootstrapServers      the bootstrap servers for the Kafka cluster.
 * @param protoConfig           the configuration for Protobuf serialization.
 * @param decodeInputAsKeyValue whether to decode the input as key-value pairs.
 * @param headers               the headers to be included in the Kafka message.
 * @param batchSize             the batch size for publishing messages.
 * @param producerConfig        the configuration for the Kafka producer.
 */
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

/**
 * Represents an HTTP output.
 *
 * @param url         the URL to which the data will be sent.
 * @param method      the HTTP method to be used (POST, PUT, etc.).
 * @param parallelism the level of parallelism for sending data. Defaults to 1.
 * @param headers     the headers to be included in the HTTP request. Defaults to an empty map.
 * @param contentType the content type of the HTTP request. Defaults to TextPlain.
 * @param stopOnError a flag indicating whether to stop on error. If true, the process stops when an error occurs. Defaults to true.
 */
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

/**
 * Represents a File System output.
 *
 * @param dir             the directory where the output file will be created.
 * @param filenamePattern the pattern for the filename. The current system time in milliseconds is used to format the filename.
 */
final case class FsOutput(dir: NonEmptyString, filenamePattern: NonEmptyString) extends Output {

  /**
   * Constructs the path for the output file.
   *
   * @return the path of the output file.
   */
  def path(): Path =
    Paths.get(dir.value, FilenameUtils.getName(filenamePattern.value.format(System.currentTimeMillis())))

  override def description(): String = s"File System output: path: ${path()}"
}

final case class S3Output(
  bucket: NonEmptyString,
  key: NonEmptyString,
  region: Region,
  endpoint: Option[Endpoint] = None,
  partSizeMb: PartSizeMB = PartSizeMB.unsafeFrom(5))
    extends Output {
  override def description(): String = s"S3 output: region: $region, bucket: $bucket, key: $key"
}
