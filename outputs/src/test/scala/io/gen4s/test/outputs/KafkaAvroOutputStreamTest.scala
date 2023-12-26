package io.gen4s.test.outputs

import org.apache.avro.SchemaParseException
import org.scalatest.funspec.AnyFunSpec
import org.scalatest.matchers.should.Matchers
import org.scalatest.OptionValues

import cats.data.NonEmptyList
import cats.effect.unsafe.implicits.global
import cats.effect.IO
import cats.implicits.*
import io.confluent.kafka.schemaregistry.avro.AvroSchema
import io.confluent.kafka.schemaregistry.client.CachedSchemaRegistryClient
import io.gen4s.core.generators.Variable
import io.gen4s.core.streams.GeneratorStream
import io.gen4s.core.templating.{OutputTransformer, SourceTemplate, TemplateBuilder}
import io.gen4s.core.Domain.*
import io.gen4s.generators.impl.{IntNumberGenerator, StringPatternGenerator}
import io.gen4s.outputs.{AvroConfig, KafkaAvroOutput, OutputStreamExecutor}
import io.gen4s.outputs.avro.SchemaLoader
import io.github.embeddedkafka.schemaregistry.{EmbeddedKafka, EmbeddedKafkaConfig}

import eu.timepit.refined.types.string.NonEmptyString
import vulcan.{AvroException, Codec}

class KafkaAvroOutputStreamTest
    extends AnyFunSpec
    with Matchers
    with KafkaConsumers
    with EmbeddedKafka
    with OptionValues {

  private implicit val config: EmbeddedKafkaConfig =
    EmbeddedKafkaConfig(
      kafkaPort = 9092,
      zooKeeperPort = 7001,
      schemaRegistryPort = 8081
    )

  private val kafka = BootstrapServers(s"localhost:${config.kafkaPort}")

  private val n = NumberOfSamplesToGenerate(1)

  case class PersonKey(id: Int, orgId: Int)
  case class Person(username: String, age: Option[Int])

  private given keyCodec: Codec[PersonKey] = Codec.record(
    name = "PersonKey",
    namespace = "io.gen4s"
  ) { field =>
    (field("id", _.id), field("orgId", _.orgId)).mapN(PersonKey(_, _))
  }

  private given codec: Codec[Person] = Codec.record(
    name = "Person",
    namespace = "io.gen4s"
  ) { field =>
    (field("username", _.username), field("age", _.age)).mapN(Person(_, _))
  }

  describe("Kafka Avro output stream") {

    it("Send AVRO records to kafka topic (auto schema register enabled)") {
      withRunningKafka {
        val template = SourceTemplate("""{ "username": "{{name}}", "age": {{age}} }""")

        val streams = OutputStreamExecutor.make[IO]()

        val builder = TemplateBuilder.make(
          NonEmptyList.one(template),
          List(
            StringPatternGenerator(Variable("name"), NonEmptyString.unsafeFrom("username_###")),
            IntNumberGenerator(Variable("age"), max = 50.some)
          ),
          Nil,
          Set.empty[OutputTransformer]
        )

        val output =
          KafkaAvroOutput(
            topic = Topic("person"),
            bootstrapServers = kafka,
            AvroConfig(
              schemaRegistryUrl = s"http://localhost:${config.schemaRegistryPort}",
              keySchema = None,
              valueSchema = java.io.File("./outputs/src/test/resources/person-value.avsc").some,
              autoRegisterSchemas = true
            )
          )

        val list = (for {
          _ <- streams.write(n, GeneratorStream.stream[IO](n, builder), output)
          r <- consumeAvroMessages[IO, Person](
                 output.topic,
                 kafka,
                 output.avroConfig.schemaRegistryUrl,
                 count = n.value.toLong
               )
        } yield r).unsafeRunSync()

        list.foreach(p => info(p.toString))
        list should not be empty
        val first = list.head
        first.username should include("username_")
      }
    }

    it("Send AVRO key/value records to kafka topic (auto schema register enabled)") {
      withRunningKafka {
        val template = SourceTemplate(""" {
                                        |   "key": {"id": 1, "orgId": 2},
                                        |   "value": { "username": "{{name}}", "age": {{age}} }
                                        |}""".stripMargin)

        val streams = OutputStreamExecutor.make[IO]()

        val builder = TemplateBuilder.make(
          NonEmptyList.one(template),
          List(
            StringPatternGenerator(Variable("name"), NonEmptyString.unsafeFrom("username_###")),
            IntNumberGenerator(Variable("age"), max = 50.some)
          ),
          Nil,
          Set.empty[OutputTransformer]
        )

        val output =
          KafkaAvroOutput(
            topic = Topic("person"),
            bootstrapServers = kafka,
            decodeInputAsKeyValue = true,
            avroConfig = AvroConfig(
              schemaRegistryUrl = s"http://localhost:${config.schemaRegistryPort}",
              keySchema = java.io.File("./outputs/src/test/resources/person-key.avsc").some,
              valueSchema = java.io.File("./outputs/src/test/resources/person-value.avsc").some,
              autoRegisterSchemas = true
            )
          )

        val list = (for {
          _ <- streams.write(n, GeneratorStream.stream[IO](n, builder), output)
          r <- consumeKeyValueAvroMessages[IO, Option[PersonKey], Person](
                 output.topic,
                 kafka,
                 output.avroConfig.schemaRegistryUrl,
                 count = n.value.toLong
               )
        } yield r).unsafeRunSync()

        list.foreach(p => info(p.toString))
        list should not be empty
        val (key, value) = list.head
        key.value.id shouldBe 1
        value.username should include("username_")

      }
    }

    it("Send AVRO key/value records to kafka topic (read schema from schema registry)") {
      withRunningKafka {
        val template = SourceTemplate(""" {
                                        |   "key": {"id": {{id}}, "orgId": 2},
                                        |   "value": { "username": "{{name}}", "age": {{age}} }
                                        |}""".stripMargin)

        val streams = OutputStreamExecutor.make[IO]()

        val builder = TemplateBuilder.make(
          NonEmptyList.one(template),
          List(
            StringPatternGenerator(Variable("name"), NonEmptyString.unsafeFrom("username_###")),
            IntNumberGenerator(Variable("age"), min = 1.some, max = 50.some),
            IntNumberGenerator(Variable("id"), min = 1.some, max = 50.some)
          ),
          Nil,
          Set.empty[OutputTransformer]
        )

        val output =
          KafkaAvroOutput(
            topic = Topic("person"),
            bootstrapServers = kafka,
            decodeInputAsKeyValue = true,
            avroConfig = AvroConfig(
              schemaRegistryUrl = s"http://localhost:${config.schemaRegistryPort}",
              keySchema = java.io.File("./outputs/src/test/resources/person-key.avsc").some,
              valueSchema = java.io.File("./outputs/src/test/resources/person-value.avsc").some
            )
          )

        val client = CachedSchemaRegistryClient(output.avroConfig.schemaRegistryUrl, 100)

        SchemaLoader
          .loadSchemaFromFile(new java.io.File("./outputs/src/test/resources/person-key.avsc"))
          .foreach { schema =>
            client.register("person-key", new AvroSchema(schema))
          }

        SchemaLoader.loadSchemaFromFile(output.avroConfig.valueSchema.get).foreach { schema =>
          client.register("person-value", new AvroSchema(schema))
        }

        val list = (for {
          _ <- streams.write(n, GeneratorStream.stream[IO](n, builder), output)
          r <- consumeKeyValueAvroMessages[IO, Option[PersonKey], Person](
                 output.topic,
                 kafka,
                 output.avroConfig.schemaRegistryUrl,
                 count = n.value.toLong
               )
        } yield r).unsafeRunSync()

        list.foreach(p => info(p.toString))
        list should not be empty
        val (key, value) = list.head
        key.value.id should be > 0
        value.username should include("username_")

      }
    }

    it("Send AVRO key/value records to kafka topic (encode with schema from schema registry)") {
      withRunningKafka {
        val template = SourceTemplate(""" {
                                        |   "key": {"id": {{id}}, "orgId": 2},
                                        |   "value": { "username": "{{name}}", "age": {{age}} }
                                        |}""".stripMargin)

        val streams = OutputStreamExecutor.make[IO]()

        val builder = TemplateBuilder.make(
          NonEmptyList.one(template),
          List(
            StringPatternGenerator(Variable("name"), NonEmptyString.unsafeFrom("username_###")),
            IntNumberGenerator(Variable("age"), min = 1.some, max = 50.some),
            IntNumberGenerator(Variable("id"), min = 1.some, max = 50.some)
          ),
          Nil,
          Set.empty[OutputTransformer]
        )

        val output =
          KafkaAvroOutput(
            topic = Topic("person"),
            bootstrapServers = kafka,
            decodeInputAsKeyValue = true,
            avroConfig = AvroConfig(
              schemaRegistryUrl = s"http://localhost:${config.schemaRegistryPort}"
            )
          )

        val client = CachedSchemaRegistryClient(output.avroConfig.schemaRegistryUrl, 100)

        SchemaLoader
          .loadSchemaFromFile(new java.io.File("./outputs/src/test/resources/person-key.avsc"))
          .foreach { schema =>
            client.register("person-key", new AvroSchema(schema))
          }

        SchemaLoader.loadSchemaFromFile(java.io.File("./outputs/src/test/resources/person-value.avsc")).foreach {
          schema =>
            client.register("person-value", new AvroSchema(schema))
        }

        val list = (for {
          _ <- streams.write(n, GeneratorStream.stream[IO](n, builder), output)
          r <- consumeKeyValueAvroMessages[IO, Option[PersonKey], Person](
                 output.topic,
                 kafka,
                 output.avroConfig.schemaRegistryUrl,
                 count = n.value.toLong
               )
        } yield r).unsafeRunSync()

        list.foreach(p => info(p.toString))
        list should not be empty
        val (key, value) = list.head
        key.value.id should be > 0
        value.username should include("username_")

      }
    }

    it("Send AVRO key/value records to kafka topic (simple key)") {
      withRunningKafka {
        val template = SourceTemplate(""" {
                                        |   "key": "key_{{id}}",
                                        |   "value": { "username": "{{name}}", "age": {{age}} }
                                        |}""".stripMargin)

        val streams = OutputStreamExecutor.make[IO]()

        val builder = TemplateBuilder.make(
          NonEmptyList.one(template),
          List(
            StringPatternGenerator(Variable("name"), NonEmptyString.unsafeFrom("username_###")),
            IntNumberGenerator(Variable("age"), min = 1.some, max = 50.some),
            IntNumberGenerator(Variable("id"), min = 1.some, max = 50.some)
          ),
          Nil,
          Set.empty[OutputTransformer]
        )

        val output =
          KafkaAvroOutput(
            topic = Topic("person"),
            bootstrapServers = kafka,
            decodeInputAsKeyValue = true,
            avroConfig = AvroConfig(
              schemaRegistryUrl = s"http://localhost:${config.schemaRegistryPort}"
            )
          )

        val client = CachedSchemaRegistryClient(output.avroConfig.schemaRegistryUrl, 100)

        SchemaLoader.loadSchemaFromFile(java.io.File("./outputs/src/test/resources/person-value.avsc")).foreach {
          schema =>
            client.register("person-value", new AvroSchema(schema))
        }

        val list = (for {
          _ <- streams.write(n, GeneratorStream.stream[IO](n, builder), output)
          r <- consumeAllAsMessages[IO](
                 output.topic,
                 kafka,
                 count = n.value.toLong
               )
        } yield r).unsafeRunSync()

        list.foreach(p => info(p.toString))
        list should not be empty
        val record = list.head
        record.key.value should include("key_")

      }
    }

    it("Fail with unknown schema (auto schema register enabled)") {
      withRunningKafka {
        val template = SourceTemplate("""{ "username": "{{name}}", "age": {{age}} }""")

        val streams = OutputStreamExecutor.make[IO]()

        val builder = TemplateBuilder.make(
          NonEmptyList.one(template),
          List(
            StringPatternGenerator(Variable("name"), NonEmptyString.unsafeFrom("username_###")),
            IntNumberGenerator(Variable("age"), min = 1.some, max = 50.some)
          ),
          Nil,
          Set.empty[OutputTransformer]
        )

        val output =
          KafkaAvroOutput(
            topic = Topic("person"),
            bootstrapServers = kafka,
            AvroConfig(
              schemaRegistryUrl = s"http://localhost:${config.schemaRegistryPort}",
              keySchema = java.io.File("./outputs/src/test/resources/person-key.avsc").some,
              valueSchema = java.io.File("./outputs/src/test/resources/person-key.avsc").some,
              autoRegisterSchemas = true
            )
          )

        an[AvroException] shouldBe thrownBy {
          (for {
            _ <- streams.write(n, GeneratorStream.stream[IO](n, builder), output)
            r <- consumeAvroMessages[IO, Person](
                   output.topic,
                   kafka,
                   output.avroConfig.schemaRegistryUrl,
                   count = n.value.toLong
                 )
          } yield r).unsafeRunSync()
        }

      }
    }

    it("Fail with broken schema (auto schema register enabled)") {
      withRunningKafka {
        val template = SourceTemplate("""{ "username": "{{name}}", "age": {{age}} }""")

        val streams = OutputStreamExecutor.make[IO]()

        val builder = TemplateBuilder.make(
          NonEmptyList.one(template),
          List(
            StringPatternGenerator(Variable("name"), NonEmptyString.unsafeFrom("username_###")),
            IntNumberGenerator(Variable("age"), min = 1.some, max = 50.some)
          ),
          Nil,
          Set.empty[OutputTransformer]
        )

        val output =
          KafkaAvroOutput(
            topic = Topic("person"),
            bootstrapServers = kafka,
            AvroConfig(
              schemaRegistryUrl = s"http://localhost:${config.schemaRegistryPort}",
              keySchema = java.io.File("./outputs/src/test/resources/person-key.avsc").some,
              valueSchema = java.io.File("./outputs/src/test/resources/broken-person-value.avsc").some,
              autoRegisterSchemas = true
            )
          )

        an[SchemaParseException] shouldBe thrownBy {
          (for {
            _ <- streams.write(n, GeneratorStream.stream[IO](n, builder), output)
            r <- consumeAvroMessages[IO, Person](
                   output.topic,
                   kafka,
                   output.avroConfig.schemaRegistryUrl,
                   count = n.value.toLong
                 )
          } yield r).unsafeRunSync()
        }

      }
    }

  }

}
