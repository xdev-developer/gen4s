package io.gen4s.outputs.processors.kafka

import java.nio.charset.StandardCharsets

import cats.effect.kernel.Async
import cats.implicits.*
import cats.Applicative
import io.gen4s.core.templating.Template
import io.gen4s.core.Domain
import io.gen4s.core.Domain.NumberOfSamplesToGenerate
import io.gen4s.outputs.processors.OutputProcessor
import io.gen4s.outputs.KafkaOutput

import fs2.kafka.Headers

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
      kvFun = (key, v) => produce(key.asByteArray.some, v.asByteArray),
      vFun = v => produce(none[Key], v.asByteArray)
    )
  }
}
