package io.gen4s.outputs.processors.kafka

import cats.effect.kernel.Async
import io.gen4s.core.templating.Template
import io.gen4s.core.Domain
import io.gen4s.core.Domain.NumberOfSamplesToGenerate
import io.gen4s.outputs.processors.OutputProcessor
import io.gen4s.outputs.KafkaOutput

import fs2.kafka.Headers

object KafkaOutputProcessor {

  def toKafkaHeaders(headers: Map[String, String]): Headers = {
    val list = headers.map { case (k, v) =>
      fs2.kafka.Header(k, v.getBytes)
    }.toList
    fs2.kafka.Headers(list: _*)
  }

}

class KafkaOutputProcessor[F[_]: Async] extends OutputProcessor[F, KafkaOutput] with KafkaOutputProcessorBase {

  override def process(
    n: Domain.NumberOfSamplesToGenerate,
    flow: fs2.Stream[F, Template],
    output: KafkaOutput): F[Unit] = {
    val producerSettings =
      mkProducerSettings[F, Option[Key], Value](output.bootstrapServers, output.kafkaProducerConfig)

    val groupSize = if (output.batchSize.value > n.value) output.batchSize.value else n.value
    val headers   = KafkaOutputProcessor.toKafkaHeaders(output.headers)

    flow
      .chunkN(groupSize)
      .evalMap(b => processBatch[F](b, output.topic, headers, output.decodeInputAsKeyValue))
      .through(fs2.kafka.KafkaProducer.pipe(producerSettings))
      .compile
      .drain
  }

}
