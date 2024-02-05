package io.gen4s.test.outputs

import org.scalatest.funspec.AnyFunSpec
import org.scalatest.matchers.should.Matchers
import org.scalatest.OptionValues
import org.typelevel.log4cats.slf4j.Slf4jLogger
import org.typelevel.log4cats.Logger

import cats.data.NonEmptyList
import cats.effect.{IO, Sync}
import cats.effect.unsafe.implicits.global
import cats.implicits.*
import io.confluent.kafka.schemaregistry.client.CachedSchemaRegistryClient
import io.gen4s.core.generators.Variable
import io.gen4s.core.streams.GeneratorStream
import io.gen4s.core.templating.{SourceTemplate, TemplateBuilder}
import io.gen4s.core.Domain.*
import io.gen4s.generators.impl.{IntNumberGenerator, StringPatternGenerator}
import io.gen4s.outputs.{KafkaProtobufOutput, OutputStreamExecutor, ProtobufConfig, ProtobufDescriptorConfig}
import io.gen4s.proto.PersonValue
import io.github.embeddedkafka.schemaregistry.{EmbeddedKafka, EmbeddedKafkaConfig}

import eu.timepit.refined.types.string.NonEmptyString

class KafkaProtobufOutputStreamTest
    extends AnyFunSpec
    with Matchers
    with KafkaConsumers
    with EmbeddedKafka
    with OptionValues {

  implicit def logger[F[_]: Sync]: Logger[F] = Slf4jLogger.getLogger[F]

  private implicit val config: EmbeddedKafkaConfig =
    EmbeddedKafkaConfig(
      kafkaPort = 9092,
      zooKeeperPort = 7001,
      schemaRegistryPort = 8081
    )

  private val kafka = BootstrapServers(s"localhost:${config.kafkaPort}")
  private val n     = NumberOfSamplesToGenerate(5)

  describe("Kafka Protobuf output stream") {

    it("Send Protobuf records to kafka topic") {
      withRunningKafka {
        val template = SourceTemplate("""{ "username": "{{name}}", "age": {{age}} }""")

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
              schemaRegistryUrl = s"http://localhost:${config.schemaRegistryPort}",
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
                 s"http://localhost:${config.schemaRegistryPort}",
                 count = n.value.toLong
               )
        } yield r).unsafeRunSync()

        list.foreach(p => println(p))

        list should not be empty
      }
    }
  }
}
