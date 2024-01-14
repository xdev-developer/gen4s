package io.gen4s.outputs.processors.kafka

import org.apache.avro.Schema

import cats.effect.kernel.{Async, Resource}
import cats.implicits.*
import cats.Applicative
import io.circe.ParsingFailure
import io.confluent.kafka.schemaregistry.client.CachedSchemaRegistryClient
import io.gen4s.core.templating.Template
import io.gen4s.core.Domain
import io.gen4s.core.Domain.NumberOfSamplesToGenerate
import io.gen4s.outputs.avro.{AvroCodec, AvroDynamicKey, AvroDynamicValue, SchemaLoader}
import io.gen4s.outputs.processors.OutputProcessor
import io.gen4s.outputs.KafkaAvroOutput

import fs2.kafka.{KeySerializer, Serializer, ValueSerializer}
import vulcan.Avro
import vulcan.Codec.Aux

class KafkaAvroOutputProcessor[F[_]: Async] extends OutputProcessor[F, KafkaAvroOutput] with KafkaOutputProcessorBase {

  override def process(
    n: Domain.NumberOfSamplesToGenerate,
    flow: fs2.Stream[F, Template],
    output: KafkaAvroOutput): F[Unit] = {

    val avroConfig = output.avroConfig

    val client = CachedSchemaRegistryClient(avroConfig.schemaRegistryUrl, avroConfig.registryClientMaxCacheSize)

    val keySchema = avroConfig.keySchema match {
      case Some(file) => SchemaLoader.loadSchemaFromFile(file)
      case None       => SchemaLoader.loadLatestSchemaFromRegistry(output.topic, client, "key")
    }

    val valueSchema = avroConfig.valueSchema match {
      case Some(file) => SchemaLoader.loadSchemaFromFile(file)
      case None       => SchemaLoader.loadLatestSchemaFromRegistry(output.topic, client, "value")
    }

    Async[F]
      .fromEither[Schema](valueSchema)
      .flatMap { schema =>
        import fs2.kafka.vulcan.*

        val registryClientSettings = SchemaRegistryClientSettings[F](avroConfig.schemaRegistryUrl)
          .withMaxCacheSize(avroConfig.registryClientMaxCacheSize)

        val avroSettings = AvroSettings(registryClientSettings).withAutoRegisterSchemas(avroConfig.autoRegisterSchemas)

        // Key serializer
        implicit val keySerializer: Resource[F, KeySerializer[F, AvroDynamicKey]] = keySchema match {
          case Right(value) =>
            implicit val keyCodec: Aux[Avro.Record, AvroDynamicKey] = AvroCodec.keyCodec(value)
            avroSerializer[AvroDynamicKey].forKey[F](avroSettings)

          case Left(ex) =>
            given Serializer[F, AvroDynamicKey] = Serializer.instance[F, AvroDynamicKey] { (_, _, key) =>
              Applicative[F].pure(key.bytes)
            }

            Resource.pure(Serializer[F, AvroDynamicKey])
        }

        // Value serializer
        implicit val valueCodec: Aux[Avro.Record, AvroDynamicValue] = AvroCodec.valueCodec(schema)

        implicit val valueSerializer: Resource[F, ValueSerializer[F, AvroDynamicValue]] =
          avroSerializer[AvroDynamicValue].forValue(avroSettings)

        val producerSettings =
          mkProducerSettingsResource[F, AvroDynamicKey, AvroDynamicValue](
            output.bootstrapServers,
            output.kafkaProducerConfig
          )

        val groupSize = if (output.batchSize.value < n.value) output.batchSize.value else n.value
        val headers   = KafkaOutputProcessor.toKafkaHeaders(output.headers)

        flow
          .chunkN(groupSize)
          .through(progressBar(n))
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
                            AvroDynamicKey(key.asByteArray),
                            AvroDynamicValue(v.asByteArray)
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
                        AvroDynamicKey(null),
                        AvroDynamicValue(value.render().asByteArray)
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
}
