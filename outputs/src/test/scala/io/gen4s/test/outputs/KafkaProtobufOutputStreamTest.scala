package io.gen4s.test.outputs

import org.scalatest.OptionValues
import org.scalatest.funspec.AnyFunSpec
import org.scalatest.matchers.should.Matchers
import org.typelevel.log4cats.Logger
import org.typelevel.log4cats.slf4j.Slf4jLogger

import com.dimafeng.testcontainers.{ForEachTestContainer, MultipleContainers}

import cats.data.NonEmptyList
import cats.effect.unsafe.implicits.global
import cats.effect.{IO, Sync}
import cats.implicits.*
import io.confluent.kafka.schemaregistry.client.CachedSchemaRegistryClient
import io.gen4s.core.Domain.*
import io.gen4s.core.generators.Variable
import io.gen4s.core.streams.GeneratorStream
import io.gen4s.core.templating.{SourceTemplate, TemplateBuilder}
import io.gen4s.generators.impl.{IntNumberGenerator, StringPatternGenerator}
import io.gen4s.outputs.{KafkaProtobufOutput, OutputStreamExecutor, ProtobufConfig, ProtobufDescriptorConfig}
import io.gen4s.proto.PersonValue

import eu.timepit.refined.types.string.NonEmptyString

class KafkaProtobufOutputStreamTest
    extends AnyFunSpec
    with Matchers
    with KafkaConsumers
    with ForEachTestContainer
    with KafkaSchemaRegistry
    with OptionValues {

  override val container: MultipleContainers = MultipleContainers(kafkaContainer, schemaRegistryContainer)

  given logger[F[_]: Sync]: Logger[F] = Slf4jLogger.getLogger[F]

  private def kafka = bootstrapServers
  private val n     = NumberOfSamplesToGenerate(5)

  describe("Kafka Protobuf output stream") {

    it("Send Protobuf records to kafka topic") {
      val template = SourceTemplate("""{ "username": "${name}", "age": ${age} }""")

      val streams = OutputStreamExecutor.make[IO]()

      val builder = TemplateBuilder.make(
        NonEmptyList.one(template),
        List(
          StringPatternGenerator(Variable("name"), NonEmptyString.unsafeFrom("username_###")),
          IntNumberGenerator(Variable("age"), max = 50.some)
        )
      )

      val output =
        KafkaProtobufOutput(
          topic = Topic("person"),
          bootstrapServers = kafka,
          ProtobufConfig(
            schemaRegistryUrl = getSchemaRegistryAddress,
            valueDescriptor = ProtobufDescriptorConfig(
              java.io.File("./outputs/src/test/resources/person-value.desc"),
              "Person"
            ),
            autoRegisterSchemas = true
          )
        )

      val client = CachedSchemaRegistryClient(output.protoConfig.schemaRegistryUrl, 100)

      val list = (for {
        _ <- streams.write(n, GeneratorStream.stream[IO](n, builder), output)
        _ <- IO.println(client.getLatestSchemaMetadata("person-value").getSchema)
        r <- consumeProtoMessages[IO, PersonValue.Person](
               output.topic,
               kafka,
               getSchemaRegistryAddress,
               count = n.value.toLong
             )
      } yield r).unsafeRunSync()

      list.foreach(p => println(p))

      list should not be empty
    }

    it("Send Key/value Protobuf records to kafka topic") {
      val template =
        SourceTemplate("""{ "key": ${id}, "value": { "username": "${name}", "age": ${age} } }""")

      val streams = OutputStreamExecutor.make[IO]()

      val builder = TemplateBuilder.make(
        NonEmptyList.one(template),
        List(
          IntNumberGenerator(Variable("id"), max = 50.some),
          StringPatternGenerator(Variable("name"), NonEmptyString.unsafeFrom("username_###")),
          IntNumberGenerator(Variable("age"), max = 50.some)
        )
      )

      val output =
        KafkaProtobufOutput(
          topic = Topic("person"),
          bootstrapServers = kafka,
          ProtobufConfig(
            schemaRegistryUrl = getSchemaRegistryAddress,
            valueDescriptor = ProtobufDescriptorConfig(
              java.io.File("./outputs/src/test/resources/person-value.desc"),
              "Person"
            ),
            autoRegisterSchemas = true
          ),
          decodeInputAsKeyValue = true
        )

      val client = CachedSchemaRegistryClient(output.protoConfig.schemaRegistryUrl, 100)

      val list = (for {
        _ <- streams.write(n, GeneratorStream.stream[IO](n, builder), output)
        _ <- IO.println(client.getLatestSchemaMetadata("person-value").getSchema)
        r <- consumeProtoMessages[IO, PersonValue.Person](
               output.topic,
               kafka,
               getSchemaRegistryAddress,
               count = n.value.toLong
             )
      } yield r).unsafeRunSync()

      list.foreach(p => println(p))

      list should not be empty
    }

    it("Send tombstone records to kafka topic") {
      val template =
        SourceTemplate("""{ "key": ${id}, "value": { "username": "${name}", "age": ${age} } }""")

      val streams = OutputStreamExecutor.make[IO]()

      val builder = TemplateBuilder.make(
        NonEmptyList.one(template),
        List(
          IntNumberGenerator(Variable("id"), max = 50.some),
          StringPatternGenerator(Variable("name"), NonEmptyString.unsafeFrom("username_###")),
          IntNumberGenerator(Variable("age"), max = 50.some)
        )
      )

      val output =
        KafkaProtobufOutput(
          topic = Topic("person"),
          bootstrapServers = kafka,
          ProtobufConfig(
            schemaRegistryUrl = getSchemaRegistryAddress,
            valueDescriptor = ProtobufDescriptorConfig(
              java.io.File("./outputs/src/test/resources/person-value.desc"),
              "Person"
            ),
            autoRegisterSchemas = true
          ),
          decodeInputAsKeyValue = true,
          writeTombstoneRecord = true
        )

      (for {
        _ <- streams.write(n, GeneratorStream.stream[IO](n, builder), output)
      } yield true).unsafeRunSync()
    }
  }
}
