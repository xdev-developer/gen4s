package io.gen4s.outputs.processors.kafka

import org.apache.kafka.clients.producer.ProducerConfig

import cats.effect.kernel.{Async, Sync}
import cats.effect.Resource
import cats.implicits.*
import io.circe.ParsingFailure
import io.gen4s.core.templating.{RenderedTemplate, Template}
import io.gen4s.core.Domain
import io.gen4s.core.Domain.{BootstrapServers, NumberOfSamplesToGenerate}
import io.gen4s.outputs.{KafkaOutputBase, KafkaProducerConfig}

import fs2.kafka.{Acks, KeySerializer, ProducerRecord, ProducerSettings, ValueSerializer}
import fs2.Chunk
import me.tongfei.progressbar.{ProgressBarBuilder, ProgressBarStyle}

trait KafkaOutputProcessorBase {

  protected type Key   = Array[Byte]
  protected type Value = Array[Byte]

  protected def mkProducerSettings[F[_]: Async, K, V](bootstrapServers: BootstrapServers, conf: KafkaProducerConfig)(
    using
    keySerializer: KeySerializer[F, K],
    valueSerializer: ValueSerializer[F, V]): ProducerSettings[F, K, V] = {
    given keySerializerR: Resource[F, KeySerializer[F, K]]     = Resource.pure(keySerializer)
    given valueSerializerR: Resource[F, ValueSerializer[F, V]] = Resource.pure(valueSerializer)

    mkProducerSettingsResource(bootstrapServers, conf)
  }

  protected def mkProducerSettingsResource[F[_]: Async, K, V](
    bootstrapServers: BootstrapServers,
    conf: KafkaProducerConfig)(using
    keySerializer: Resource[F, KeySerializer[F, K]],
    valueSerializer: Resource[F, ValueSerializer[F, V]]): ProducerSettings[F, K, V] = {
    fs2.kafka
      .ProducerSettings(
        keySerializer = keySerializer,
        valueSerializer = valueSerializer
      )
      .withBootstrapServers(bootstrapServers.value)
      .withClientId("gen4s")
      .withAcks(Acks.All)
      .withBatchSize(conf.maxBatchSizeBytes)
      .withMaxInFlightRequestsPerConnection(conf.inFlightRequests)
      .withProperties(
        (ProducerConfig.COMPRESSION_TYPE_CONFIG, conf.compressionType.entryName),
        (ProducerConfig.LINGER_MS_CONFIG, conf.lingerMs.toString),
        (ProducerConfig.MAX_REQUEST_SIZE_CONFIG, conf.maxRequestSizeBytes.toString)
      )

  }

  protected def runStream[F[_]: Async, K, V](
    n: NumberOfSamplesToGenerate,
    flow: fs2.Stream[F, Template],
    output: KafkaOutputBase,
    producerSettings: ProducerSettings[F, K, V],
    keyValueMapper: (Array[Byte], RenderedTemplate) => F[ProducerRecord[K, V]],
    valueMapper: RenderedTemplate => F[ProducerRecord[K, V]]
  ): F[Unit] = {

    val groupSize = if (output.batchSize.value < n.value) output.batchSize.value else n.value
    flow
      .chunkN(groupSize)
      .through(progressInfo(n))
      .evalMap { batch =>
        batch
          .map { value =>
            if (output.decodeInputAsKeyValue) {
              value.render().asKeyValue match {
                case Right((key, v)) => keyValueMapper(key, v)
                case Left(ex)        =>
                  Async[F].raiseError(ParsingFailure(s"Template key/value parsing failure: ${ex.message}", ex))
              }

            } else { // No key usage
              valueMapper(value.render())
            }
          }
          .sequence
          .map(fs2.kafka.ProducerRecords.apply)
      }
      .through(fs2.kafka.KafkaProducer.pipe(producerSettings))
      .compile
      .drain
  }

  private def progressInfo[F[_]: Sync, V](n: Domain.NumberOfSamplesToGenerate): fs2.Pipe[F, Chunk[V], Chunk[V]] =
    if (n.value >= 10_000) progressBar(n) else passThrough()

  private def passThrough[F[_]: Sync, V](): fs2.Pipe[F, Chunk[V], Chunk[V]] = src => src

  private def progressBar[F[_]: Sync, V](n: Domain.NumberOfSamplesToGenerate): fs2.Pipe[F, Chunk[V], Chunk[V]] =
    src =>
      val pb = Sync[F].delay {
        ProgressBarBuilder()
          .setTaskName("Sending records")
          .setInitialMax(n.value)
          .setStyle(ProgressBarStyle.COLORFUL_UNICODE_BAR)
          .build()
      }
      fs2.Stream
        .bracket(pb)(p => Sync[F].delay(p.close()))
        .flatMap { pb =>
          src.evalMap(l => Sync[F].delay(pb.stepBy(l.size.toLong)).as(l))
        }

}
