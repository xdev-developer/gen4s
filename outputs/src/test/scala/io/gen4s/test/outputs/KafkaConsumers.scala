package io.gen4s.test.outputs

import java.util.UUID

import cats.effect.kernel.Async
import io.gen4s.core.Domain.*

import scala.concurrent.duration.*

import fs2.kafka.*

case class Message(value: String, headers: Option[Headers] = None)

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
      .withGroupId(UUID.randomUUID().toString)

    KafkaConsumer
      .stream(consumerSettings)
      .subscribeTo(topic.value)
      .records
      .take(count)
      .map(r => Message(r.record.value, Option(r.record.headers)))
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
      .withGroupId(UUID.randomUUID().toString)

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

}
