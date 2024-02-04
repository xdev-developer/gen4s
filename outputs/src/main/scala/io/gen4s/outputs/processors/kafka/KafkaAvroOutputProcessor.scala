package io.gen4s.outputs.processors.kafka

import org.apache.avro.Schema
import org.typelevel.log4cats.Logger

import cats.effect.kernel.{Async, Resource}
import cats.implicits.*
import cats.Applicative
import io.confluent.kafka.schemaregistry.client.CachedSchemaRegistryClient
import io.gen4s.core.templating.{RenderedTemplate, Template}
import io.gen4s.core.Domain
import io.gen4s.core.Domain.{NumberOfSamplesToGenerate, Topic}
import io.gen4s.outputs.avro.{AvroCodec, AvroDynamicKey, AvroDynamicValue, SchemaLoader}
import io.gen4s.outputs.processors.OutputProcessor
import io.gen4s.outputs.KafkaAvroOutput

import fs2.kafka.{KeySerializer, ProducerRecord, Serializer, ValueSerializer}
import fs2.kafka.vulcan.{AvroSettings, SchemaRegistryClient}
import vulcan.Avro
import vulcan.Codec.Aux

class KafkaAvroOutputProcessor[F[_]: Async: Logger]
    extends OutputProcessor[F, KafkaAvroOutput]
    with KafkaOutputProcessorBase {

  override def process(
    n: Domain.NumberOfSamplesToGenerate,
    flow: fs2.Stream[F, Template],
    output: KafkaAvroOutput): F[Unit] = {

    val avroConfig = output.avroConfig
    val client     = CachedSchemaRegistryClient(avroConfig.schemaRegistryUrl, avroConfig.registryClientMaxCacheSize)

    (loadKeySchema(output, client), loadValueSchema(output, client))
      .flatMapN { case (keySchema, valueSchema) =>
        import fs2.kafka.vulcan.*

        val registryClientSettings = SchemaRegistryClientSettings[F](avroConfig.schemaRegistryUrl)
          .withMaxCacheSize(avroConfig.registryClientMaxCacheSize)

        val avroSettings = AvroSettings(registryClientSettings)
          .withAutoRegisterSchemas(avroConfig.autoRegisterSchemas)

        // Key serializer
        given Resource[F, KeySerializer[F, AvroDynamicKey]] =
          makeKeySerializer(keySchema, avroSettings)

        // Value serializer
        given Aux[Avro.Record, AvroDynamicValue] = AvroCodec.valueCodec(valueSchema)
        given Resource[F, ValueSerializer[F, AvroDynamicValue]] =
          avroSerializer[AvroDynamicValue].forValue(avroSettings)

        val producerSettings =
          mkProducerSettingsResource[F, AvroDynamicKey, AvroDynamicValue](
            output.bootstrapServers,
            output.kafkaProducerConfig
          )

        val headers = KafkaOutputProcessor.toKafkaHeaders(output.headers)

        def produce(key: AvroDynamicKey, value: AvroDynamicValue) = {
          Applicative[F].pure(fs2.kafka.ProducerRecord(output.topic.value, key, value).withHeaders(headers))
        }

        runStream[F, AvroDynamicKey, AvroDynamicValue](
          n,
          flow,
          output,
          producerSettings,
          kvFun = (key, v) => produce(AvroDynamicKey(key.asByteArray), AvroDynamicValue(v.asByteArray)),
          vFun = v => produce(AvroDynamicKey(null), AvroDynamicValue(v.asByteArray))
        )
      }
  }

  private def loadKeySchema(output: KafkaAvroOutput, client: SchemaRegistryClient): F[Option[Schema]] = {
    if (output.decodeInputAsKeyValue) {
      output.avroConfig.keySchema match {
        case Some(file) =>
          Logger[F].info(s"Loading key-schema from file ${file.getAbsolutePath}") *>
            Async[F].fromEither(SchemaLoader.loadSchemaFromFile(file)).map(_.some)

        case None =>
          Logger[F].info(s"Looking for key-schema in schema-registry") *>
            Async[F]
              .fromEither(SchemaLoader.loadLatestSchemaFromRegistry(output.topic, client, "key"))
              .map(_.some)
              .recover(_ => None)
      }
    } else Async[F].pure(None)
  }

  private def loadValueSchema(output: KafkaAvroOutput, client: SchemaRegistryClient): F[Schema] = {
    output.avroConfig.valueSchema match {
      case Some(file) =>
        Logger[F].info(s"Loading value-schema from file ${file.getAbsolutePath}") *>
          Async[F].fromEither(SchemaLoader.loadSchemaFromFile(file))

      case None =>
        Logger[F].info(s"Loading value-schema from registry") *>
          Async[F].fromEither(SchemaLoader.loadLatestSchemaFromRegistry(output.topic, client, "value"))
    }
  }

  private def makeKeySerializer(
    keySchema: Option[Schema],
    avroSettings: AvroSettings[F]): Resource[F, KeySerializer[F, AvroDynamicKey]] = {
    keySchema match {
      case Some(value) =>
        given Aux[Avro.Record, AvroDynamicKey] = AvroCodec.keyCodec(value)
        fs2.kafka.vulcan
          .avroSerializer[AvroDynamicKey]
          .forKey[F](avroSettings)

      case None =>
        // Pass key as is without Avro encoding
        given Serializer[F, AvroDynamicKey] = Serializer
          .instance[F, AvroDynamicKey] { (_, _, key) =>
            Applicative[F].pure(key.bytes)
          }

        Resource.pure(Serializer[F, AvroDynamicKey])
    }
  }
}
