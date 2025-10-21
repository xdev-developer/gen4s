package io.gen4s.outputs.processors.kafka

import java.nio.charset.StandardCharsets

import cats.Applicative
import cats.effect.kernel.Async
import cats.implicits.*
import io.gen4s.core.Domain
import io.gen4s.core.templating.Template
import io.gen4s.outputs.KafkaOutput
import io.gen4s.outputs.processors.OutputProcessor

import fs2.kafka.{Headers, Serializer, ValueSerializer}

object KafkaOutputProcessor {

  def toKafkaHeaders(headers: Map[String, String]): Headers = {
    val list = headers.map { case (k, v) =>
      fs2.kafka.Header(k, v.getBytes(StandardCharsets.UTF_8))
    }.toList
    fs2.kafka.Headers(list*)
  }

}

class KafkaOutputProcessor[F[_]: Async] extends OutputProcessor[F, KafkaOutput] with KafkaOutputProcessorBase {

  override def process(
    n: Domain.NumberOfSamplesToGenerate,
    flow: fs2.Stream[F, Template],
    output: KafkaOutput): F[Unit] = {

    given ValueSerializer[F, Value] =
      makeValueSerializer(writeTombstoneRecord = output.isTombstoneOutput)

    val producerSettings =
      mkProducerSettings[F, Option[Key], Value](output.bootstrapServers, output.kafkaProducerConfig)

    val headers = KafkaOutputProcessor.toKafkaHeaders(output.headers)

    def produce(key: Option[Key], value: Value) = {
      Applicative[F].pure(fs2.kafka.ProducerRecord(output.topic.value, key, value).withHeaders(headers))
    }

    runStream[F, Option[Key], Value](
      n,
      flow,
      output,
      producerSettings,
      keyValueMapper = (key, v) => produce(key.some, v.asByteArray),
      valueMapper = v => produce(none[Key], v.asByteArray)
    )
  }

  private def makeValueSerializer(writeTombstoneRecord: Boolean) = {
    if (writeTombstoneRecord) {
      Serializer.asNull[F, Value]
    } else Serializer.identity[F]
  }
}
