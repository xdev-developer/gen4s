package io.gen4s.outputs.processors.kafka

import org.apache.kafka.clients.producer.ProducerConfig

import cats.effect.kernel.{Async, Sync}
import cats.effect.Resource
import cats.implicits.*
import cats.Applicative
import io.circe.ParsingFailure
import io.gen4s.core.templating.Template
import io.gen4s.core.Domain
import io.gen4s.core.Domain.{BootstrapServers, Topic}
import io.gen4s.outputs.KafkaProducerConfig

import fs2.kafka.{Acks, Headers, KeySerializer, ProducerRecords, ProducerSettings, ValueSerializer}
import fs2.Chunk
import me.tongfei.progressbar.ProgressBar

trait KafkaOutputProcessorBase {

  protected type Key   = Array[Byte]
  protected type Value = Array[Byte]

  protected def processBatch[F[_]: Async](
    batch: Chunk[Template],
    topic: Topic,
    headers: Headers,
    decodeInputAsKeyValue: Boolean): F[ProducerRecords[Option[Key], Value]] = {
    batch
      .map { value =>
        if (decodeInputAsKeyValue) {
          value.render().asKeyValue match {
            case Right((key, v)) =>
              Applicative[F].pure(
                fs2.kafka
                  .ProducerRecord(topic.value, key.asByteArray.some, v.asByteArray)
                  .withHeaders(headers)
              )

            case Left(ex) =>
              Async[F].raiseError(ParsingFailure(s"Template key/value parsing failure: ${ex.message}", ex))
          }

        } else {
          Applicative[F].pure(
            fs2.kafka
              .ProducerRecord(topic.value, none[Key], value.render().asByteArray)
              .withHeaders(headers)
          )
        }
      }
      .sequence
      .map(fs2.kafka.ProducerRecords.apply)
  }

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

  protected def progressBar[F[_]: Sync, V](n: Domain.NumberOfSamplesToGenerate): fs2.Pipe[F, Chunk[V], Chunk[V]] =
    src =>
      val pb = Sync[F].delay(new ProgressBar("Processing", n.value))
      fs2.Stream
        .bracket(pb)(p => Sync[F].delay(p.close()))
        .flatMap { pb =>
          src.evalMap(l => Sync[F].delay(pb.stepBy(l.size.toLong)).as(l))
        }

}
