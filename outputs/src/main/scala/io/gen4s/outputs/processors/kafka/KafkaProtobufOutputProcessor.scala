package io.gen4s.outputs.processors.kafka

import org.typelevel.log4cats.Logger

import com.google.protobuf.Descriptors.Descriptor
import com.google.protobuf.DynamicMessage

import cats.effect.kernel.{Async, Resource}
import cats.implicits.*
import io.gen4s.core.templating.{RenderedTemplate, Template}
import io.gen4s.core.Domain
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
          mkProducerSettingsResource[F, Option[Key], DynamicMessage](
            output.bootstrapServers,
            output.kafkaProducerConfig
          )

        val headers = KafkaOutputProcessor.toKafkaHeaders(output.headers)

        def produce(key: Option[Key], value: RenderedTemplate, descriptor: Descriptor) = {
          Async[F]
            .catchNonFatal(ProtobufConverter.toDynamicRecord(descriptor, value))
            .map(msg => fs2.kafka.ProducerRecord(output.topic.value, key, msg).withHeaders(headers))
        }

        runStream[F, Option[Key], DynamicMessage](
          n,
          flow,
          output,
          producerSettings,
          keyValueMapper = (key, v) => produce(key.asByteArray.some, v, descriptor),
          valueMapper = v => produce(none[Key], v, descriptor)
        )
      }
  }

  private def loadValueDescriptor(cfg: ProtobufDescriptorConfig): F[Descriptor] = {
    Logger[F].info(s"Loading ${cfg.messageType} value-descriptor from file ${cfg.file.getAbsolutePath}") *>
      ProtobufConverter.loadDescriptor(cfg)
  }
}
