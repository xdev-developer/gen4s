package io.gen4s.test.outputs

import java.util.UUID

import scala.reflect.ClassTag

import cats.effect.kernel.{Async, Resource}
import io.confluent.kafka.schemaregistry.protobuf.ProtobufSchemaProvider
import io.gen4s.core.Domain.*
import io.gen4s.outputs.protobuf.{protobufDeserializer, ProtobufSettings}

import scala.concurrent.duration.*

import _root_.vulcan.Codec
import fs2.kafka.*
import fs2.kafka.vulcan.CachedSchemaRegistryClient

final case class Message(key: Option[String], value: String, headers: Option[Headers] = None)

trait KafkaConsumers {

  def consumeAllAsMessages[F[_]: Async](
    topic: Topic,
    bootstrapServer: BootstrapServers,
    count: Long = 100L): F[List[Message]] = {
    val consumerSettings = ConsumerSettings(
      keyDeserializer = Deserializer[F, Option[String]],
      valueDeserializer = Deserializer[F, String]
    ).withAutoOffsetReset(AutoOffsetReset.Earliest)
      .withBootstrapServers(bootstrapServer.value)
      .withGroupId(String.valueOf(UUID.randomUUID()))

    KafkaConsumer
      .stream(consumerSettings)
      .subscribeTo(topic.value)
      .records
      .take(count)
      .map(r => Message(r.record.key, r.record.value, Option(r.record.headers)))
      .interruptAfter(60.seconds)
      .compile
      .toList
  }

  def consumeAllAsByteArray[F[_]: Async](
    topic: Topic,
    bootstrapServer: BootstrapServers,
    count: Long = 100L): F[List[Array[Byte]]] = {
    val consumerSettings = ConsumerSettings(
      keyDeserializer = Deserializer[F, Option[Array[Byte]]],
      valueDeserializer = Deserializer[F, Array[Byte]]
    ).withAutoOffsetReset(AutoOffsetReset.Earliest)
      .withBootstrapServers(bootstrapServer.value)
      .withGroupId(String.valueOf(UUID.randomUUID()))

    KafkaConsumer
      .stream(consumerSettings)
      .subscribeTo(topic.value)
      .records
      .take(count)
      .map(r => r.record.value)
      .interruptAfter(60.seconds)
      .compile
      .toList
  }

  def consumeAvroMessages[F[_]: Async, T](
    topic: Topic,
    bootstrapServer: BootstrapServers,
    registryUrl: String,
    count: Long = 100L)(using c: Codec[T]): F[List[T]] = {

    import fs2.kafka.vulcan.{AvroSettings, SchemaRegistryClientSettings, avroDeserializer}

    val avroSettings = AvroSettings(SchemaRegistryClientSettings[F](registryUrl))

    implicit val entityDeserializer: Resource[F, ValueDeserializer[F, T]] = avroDeserializer[T].forValue(avroSettings)

    val consumerSettings = ConsumerSettings(
      keyDeserializer = Deserializer[F, Option[String]],
      valueDeserializer = entityDeserializer
    ).withAutoOffsetReset(AutoOffsetReset.Earliest)
      .withBootstrapServers(bootstrapServer.value)
      .withGroupId(String.valueOf(UUID.randomUUID()))

    KafkaConsumer
      .stream(consumerSettings)
      .subscribeTo(topic.value)
      .records
      .take(count)
      .map(r => r.record.value)
      .interruptAfter(60.seconds)
      .compile
      .toList
  }

  def consumeKeyValueAvroMessages[F[_]: Async, K, V](
    topic: Topic,
    bootstrapServer: BootstrapServers,
    registryUrl: String,
    count: Long = 100L)(using k: Codec[K], v: Codec[V]): F[List[(K, V)]] = {

    import fs2.kafka.vulcan.{AvroSettings, SchemaRegistryClientSettings, avroDeserializer}

    val avroSettings = AvroSettings(SchemaRegistryClientSettings[F](registryUrl))

    implicit val keyDeserializer: Resource[F, KeyDeserializer[F, K]] =
      avroDeserializer[K].forKey(avroSettings)

    implicit val entityDeserializer: Resource[F, ValueDeserializer[F, V]] = avroDeserializer[V].forValue(avroSettings)

    val consumerSettings = ConsumerSettings(
      keyDeserializer = keyDeserializer,
      valueDeserializer = entityDeserializer
    ).withAutoOffsetReset(AutoOffsetReset.Earliest)
      .withBootstrapServers(bootstrapServer.value)
      .withGroupId(String.valueOf(UUID.randomUUID()))

    KafkaConsumer
      .stream(consumerSettings)
      .subscribeTo(topic.value)
      .records
      .take(count)
      .map(r => (r.record.key, r.record.value))
      .interruptAfter(60.seconds)
      .compile
      .toList
  }

  @SuppressWarnings(Array("org.wartremover.warts.Null"))
  def consumeProtoMessages[F[_]: Async, T <: com.google.protobuf.Message: ClassTag](
    topic: Topic,
    bootstrapServer: BootstrapServers,
    registryUrl: String,
    count: Long = 100L): F[List[(Option[String], T)]] = {

    import scala.jdk.CollectionConverters.*

    val client = new CachedSchemaRegistryClient(
      registryUrl,
      1000,
      List(new ProtobufSchemaProvider()).asJava,
      null
    )

    val protoSettings = ProtobufSettings[F, T](client)

    implicit val entityDeserializer: Resource[F, ValueDeserializer[F, T]] =
      protobufDeserializer[T].forValue(protoSettings)

    val consumerSettings = ConsumerSettings(
      keyDeserializer = Deserializer[F, Option[String]],
      valueDeserializer = entityDeserializer
    ).withAutoOffsetReset(AutoOffsetReset.Earliest)
      .withBootstrapServers(bootstrapServer.value)
      .withGroupId(String.valueOf(UUID.randomUUID()))

    KafkaConsumer
      .stream(consumerSettings)
      .subscribeTo(topic.value)
      .records
      .take(count)
      .map(r => (r.record.key, r.record.value))
      .interruptAfter(60.seconds)
      .compile
      .toList
  }

}
