package io.gen4s.outputs.processors.kafka

import org.typelevel.log4cats.Logger

import com.google.protobuf.Descriptors.Descriptor
import com.google.protobuf.DynamicMessage

import cats.effect.kernel.{Async, Resource}
import cats.implicits.*
import cats.Applicative
import io.circe.ParsingFailure
import io.gen4s.core.templating.Template
import io.gen4s.core.Domain
import io.gen4s.core.Domain.NumberOfSamplesToGenerate
import io.gen4s.outputs.{KafkaProtobufOutput, ProtobufDescriptorConfig}
import io.gen4s.outputs.processors.OutputProcessor
import io.gen4s.outputs.protobuf.*

import fs2.kafka.vulcan.SchemaRegistryClientSettings
import fs2.kafka.ValueSerializer

class KafkaProtobufOutputProcessor[F[_]: Async: Logger]
    extends OutputProcessor[F, KafkaProtobufOutput]
    with KafkaOutputProcessorBase {

  override def process(
    n: Domain.NumberOfSamplesToGenerate,
    flow: fs2.Stream[F, Template],
    output: KafkaProtobufOutput): F[Unit] = {

    val protoConfig = output.protoConfig

    loadValueDescriptor(protoConfig.valueDescriptor)
      .flatMap { descriptor =>

        val registryClientSettings = SchemaRegistryClientSettings[F](protoConfig.schemaRegistryUrl)
          .withMaxCacheSize(protoConfig.registryClientMaxCacheSize)

        val protoSettings = ProtobufSettings[F, DynamicMessage](registryClientSettings)
          .withAutoRegisterSchemas(protoConfig.autoRegisterSchemas)

        given Resource[F, ValueSerializer[F, DynamicMessage]] =
          protobufSerializer[DynamicMessage].forValue(protoSettings)

        val producerSettings =
          mkProducerSettingsResource[F, Array[Byte], DynamicMessage](
            output.bootstrapServers,
            output.kafkaProducerConfig
          )

        val groupSize = if (output.batchSize.value < n.value) output.batchSize.value else n.value
        val headers   = KafkaOutputProcessor.toKafkaHeaders(output.headers)

        flow
          .chunkN(groupSize)
          .evalMap { batch =>
            batch
              .map { value =>
                if (output.decodeInputAsKeyValue) {
                  value.render().asKeyValue match {
                    case Right((key, v)) =>
                      Applicative[F].pure(
                        fs2.kafka
                          .ProducerRecord(
                            output.topic.value,
                            key.asByteArray,
                            ProtobufConverter.toDynamicRecord(descriptor, v)
                          )
                          .withHeaders(headers)
                      )

                    case Left(ex) =>
                      Async[F].raiseError(ParsingFailure(s"Template key/value parsing failure: ${ex.message}", ex))
                  }

                } else {
                  Applicative[F].pure(
                    fs2.kafka
                      .ProducerRecord(
                        output.topic.value,
                        null,
                        ProtobufConverter.toDynamicRecord(descriptor, value.render())
                      )
                      .withHeaders(headers)
                  )
                }
              }
              .sequence
              .map(fs2.kafka.ProducerRecords.apply)
          }
          .through(fs2.kafka.KafkaProducer.pipe(producerSettings))
          .compile
          .drain
      }
  }

  private def loadValueDescriptor(cfg: ProtobufDescriptorConfig): F[Descriptor] = {
    Logger[F].info(s"Loading ${cfg.messageType} value-descriptor from file ${cfg.file.getAbsolutePath}") *>
      ProtobufConverter.loadDescriptor(cfg)
  }
}
