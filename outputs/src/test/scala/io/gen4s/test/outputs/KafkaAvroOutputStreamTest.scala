package io.gen4s.test.outputs

import java.io.File
import java.time.Instant

import org.apache.avro.SchemaParseException
import org.scalatest.funspec.AnyFunSpec
import org.scalatest.matchers.should.Matchers
import org.scalatest.OptionValues
import org.typelevel.log4cats.slf4j.Slf4jLogger
import org.typelevel.log4cats.Logger

import com.dimafeng.testcontainers.{ForEachTestContainer, MultipleContainers}

import cats.data.NonEmptyList
import cats.effect.{IO, Sync}
import cats.effect.unsafe.implicits.global
import cats.implicits.*
import cats.Show
import io.confluent.kafka.schemaregistry.avro.AvroSchema
import io.confluent.kafka.schemaregistry.client.rest.exceptions.RestClientException
import io.confluent.kafka.schemaregistry.client.CachedSchemaRegistryClient
import io.gen4s.core.generators.Variable
import io.gen4s.core.streams.GeneratorStream
import io.gen4s.core.templating.{SourceTemplate, TemplateBuilder}
import io.gen4s.core.Domain.*
import io.gen4s.generators.impl.{IntNumberGenerator, StringPatternGenerator}
import io.gen4s.outputs.{AvroConfig, KafkaAvroOutput, OutputStreamExecutor}
import io.gen4s.outputs.avro.SchemaLoader

import eu.timepit.refined.types.string.NonEmptyString
import vulcan.{AvroException, Codec}

class KafkaAvroOutputStreamTest
    extends AnyFunSpec
    with Matchers
    with KafkaConsumers
    with ForEachTestContainer
    with KafkaSchemaRegistry
    with OptionValues {

  override val container: MultipleContainers = MultipleContainers(kafkaContainer, schemaRegistryContainer)

  implicit def logger[F[_]: Sync]: Logger[F] = Slf4jLogger.getLogger[F]

  private val n            = NumberOfSamplesToGenerate(1)
  private def kafkaServers = bootstrapServers

  case class PersonKey(id: Int, orgId: Int)
  case class Person(username: String, age: Option[Int], birthDate: Instant)

  private given Show[Person] = Show.fromToString[Person]

  given keyCodec: Codec[PersonKey] = Codec.record(
    name = "PersonKey",
    namespace = "io.gen4s"
  ) { field =>
    (field("id", _.id), field("orgId", _.orgId)).mapN(PersonKey(_, _))
  }

  private given Codec[Person] = Codec.record(
    name = "Person",
    namespace = "io.gen4s"
  ) { field =>
    (field("username", _.username), field("age", _.age), field("birthDate", _.birthDate)).mapN(Person(_, _, _))
  }

  private val personTemplate = """{ "username": "{{name}}", "age": {{age}}, "birthDate": "2007-12-03T10:15:30.999Z" }"""

  private def mkOutput(
    b: BootstrapServers,
    registryUrl: String,
    keySchema: Option[File] = None,
    valueSchema: Option[File] = None,
    autoRegisterSchemas: Boolean = false) =
    KafkaAvroOutput(
      topic = Topic("person"),
      bootstrapServers = b,
      AvroConfig(
        schemaRegistryUrl = registryUrl,
        keySchema = keySchema,
        valueSchema = valueSchema,
        autoRegisterSchemas = autoRegisterSchemas
      )
    )

  private def runStream(
    bootstrapServers: BootstrapServers,
    streams: OutputStreamExecutor[IO],
    builder: TemplateBuilder,
    output: KafkaAvroOutput) = {
    (for {
      _ <- streams.write(n, GeneratorStream.stream[IO](n, builder), output)
      r <- consumeAvroMessages[IO, Person](
             output.topic,
             bootstrapServers,
             output.avroConfig.schemaRegistryUrl,
             count = n.value.toLong
           )
    } yield r).unsafeRunSync()
  }

  describe("Kafka Avro output stream") {

    it("Send AVRO records to kafka topic (auto schema register enabled)") {
      val template = SourceTemplate(personTemplate)

      val streams = OutputStreamExecutor.make[IO]()

      val builder = TemplateBuilder.make(
        NonEmptyList.one(template),
        List(
          StringPatternGenerator(Variable("name"), NonEmptyString.unsafeFrom("username_###")),
          IntNumberGenerator(Variable("age"), max = 50.some)
        )
      )

      val output = mkOutput(
        kafkaServers,
        getSchemaRegistryAddress,
        valueSchema = java.io.File("./outputs/src/test/resources/person-value.avsc").some,
        autoRegisterSchemas = true
      )

      val list = runStream(kafkaServers, streams, builder, output)

      list.foreach(p => info(p.show))
      list should not be empty
      list.headOption.map(_.username).value should include("username_")
    }

    it("Send AVRO key/value records to kafka topic (auto schema register enabled)") {
      val template = SourceTemplate(s""" {
                                       |   "key": {"id": 1, "orgId": 2},
                                       |   "value": $personTemplate
                                       |}""".stripMargin)

      val streams = OutputStreamExecutor.make[IO]()

      val builder = TemplateBuilder.make(
        NonEmptyList.one(template),
        List(
          StringPatternGenerator(Variable("name"), NonEmptyString.unsafeFrom("username_###")),
          IntNumberGenerator(Variable("age"), max = 50.some)
        )
      )

      val output = mkOutput(
        kafkaServers,
        getSchemaRegistryAddress,
        keySchema = java.io.File("./outputs/src/test/resources/person-key.avsc").some,
        valueSchema = java.io.File("./outputs/src/test/resources/person-value.avsc").some,
        autoRegisterSchemas = true
      ).copy(decodeInputAsKeyValue = true)

      val list = (for {
        _ <- streams.write(n, GeneratorStream.stream[IO](n, builder), output)
        r <- consumeKeyValueAvroMessages[IO, Option[PersonKey], Person](
               output.topic,
               kafkaServers,
               output.avroConfig.schemaRegistryUrl,
               count = n.value.toLong
             )
      } yield r).unsafeRunSync()

      list.foreach(p => info(s"$p"))
      list should not be empty
      val (key, value) = list.headOption.value
      key.value.id shouldBe 1
      value.username should include("username_")
    }

    it("Send AVRO key/value records to kafka topic (encode with schema from schema registry)") {
      val template = SourceTemplate(s""" {
                                       |   "key": {"id": {{id}}, "orgId": 2},
                                       |   "value": $personTemplate
                                       |}""".stripMargin)

      val streams = OutputStreamExecutor.make[IO]()

      val builder = TemplateBuilder.make(
        NonEmptyList.one(template),
        List(
          StringPatternGenerator(Variable("name"), NonEmptyString.unsafeFrom("username_###")),
          IntNumberGenerator(Variable("age"), min = 1.some, max = 50.some),
          IntNumberGenerator(Variable("id"), min = 1.some, max = 50.some)
        )
      )

      val output =
        KafkaAvroOutput(
          topic = Topic("person"),
          bootstrapServers = kafkaServers,
          decodeInputAsKeyValue = true,
          avroConfig = AvroConfig(
            schemaRegistryUrl = getSchemaRegistryAddress
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
               kafkaServers,
               output.avroConfig.schemaRegistryUrl,
               count = n.value.toLong
             )
      } yield r).unsafeRunSync()

      list.foreach(p => info(s"$p"))
      list should not be empty
      val (key, value) = list.headOption.value
      key.value.id should be > 0
      value.username should include("username_")
    }

    it("Send AVRO key/value records to kafka topic (simple key)") {
      val template = SourceTemplate(s""" {
                                       |   "key": "key_{{id}}",
                                       |   "value": $personTemplate
                                       |}""".stripMargin)

      val streams = OutputStreamExecutor.make[IO]()

      val builder = TemplateBuilder.make(
        NonEmptyList.one(template),
        List(
          StringPatternGenerator(Variable("name"), NonEmptyString.unsafeFrom("username_###")),
          IntNumberGenerator(Variable("age"), min = 1.some, max = 50.some),
          IntNumberGenerator(Variable("id"), min = 1.some, max = 50.some)
        )
      )

      val output = mkOutput(
        kafkaServers,
        getSchemaRegistryAddress
      ).copy(decodeInputAsKeyValue = true)

      val client = CachedSchemaRegistryClient(output.avroConfig.schemaRegistryUrl, 100)

      SchemaLoader.loadSchemaFromFile(java.io.File("./outputs/src/test/resources/person-value.avsc")).foreach {
        schema =>
          client.register("person-value", new AvroSchema(schema))
      }

      val list = (for {
        _ <- streams.write(n, GeneratorStream.stream[IO](n, builder), output)
        r <- consumeAllAsMessages[IO](
               output.topic,
               kafkaServers,
               count = n.value.toLong
             )
      } yield r).unsafeRunSync()

      list.foreach(p => info(s"$p"))
      list should not be empty
      val record = list.headOption.value
      record.key.value should include("key_")

    }

    it("Fail with unknown schema (auto schema register enabled)") {
      val template = SourceTemplate(s""" {
                                       |   "key": {"id": {{id}}, "orgId": 2},
                                       |   "value": $personTemplate
                                       |}""".stripMargin)

      val streams = OutputStreamExecutor.make[IO]()

      val builder = TemplateBuilder.make(
        NonEmptyList.one(template),
        List(
          StringPatternGenerator(Variable("name"), NonEmptyString.unsafeFrom("username_###")),
          IntNumberGenerator(Variable("age"), min = 1.some, max = 50.some),
          IntNumberGenerator(Variable("id"), min = 1.some, max = 50.some)
        )
      )

      val output = mkOutput(
        kafkaServers,
        getSchemaRegistryAddress,
        keySchema = java.io.File("./outputs/src/test/resources/person-key.avsc").some,
        valueSchema = java.io.File("./outputs/src/test/resources/person-key.avsc").some,
        autoRegisterSchemas = true
      ).copy(decodeInputAsKeyValue = true)

      val error = the[AvroException] thrownBy {
        runStream(kafkaServers, streams, builder, output)
      }

      error.getMessage shouldBe "Avro value encoder error: Missing INT node @ /id"
    }

    it("Fail without schema") {
      val template = SourceTemplate(s""" {
                                       |   "key": {"id": {{id}}, "orgId": 2},
                                       |   "value": $personTemplate
                                       |}""".stripMargin)

      val streams = OutputStreamExecutor.make[IO]()

      val builder = TemplateBuilder.make(
        NonEmptyList.one(template),
        List(
          StringPatternGenerator(Variable("name"), NonEmptyString.unsafeFrom("username_###")),
          IntNumberGenerator(Variable("age"), min = 1.some, max = 50.some),
          IntNumberGenerator(Variable("id"), min = 1.some, max = 50.some)
        )
      )

      val output = mkOutput(
        kafkaServers,
        getSchemaRegistryAddress
      ).copy(decodeInputAsKeyValue = true)

      val error = the[RestClientException] thrownBy {
        runStream(kafkaServers, streams, builder, output)
      }

      error.getMessage shouldBe "Subject 'person-value' not found.; error code: 40401"
    }

    it("Fail with undefined key") {
      val template = SourceTemplate(personTemplate)

      val streams = OutputStreamExecutor.make[IO]()

      val builder = TemplateBuilder.make(
        NonEmptyList.one(template),
        List(
          StringPatternGenerator(Variable("name"), NonEmptyString.unsafeFrom("username_###")),
          IntNumberGenerator(Variable("age"), min = 1.some, max = 50.some)
        )
      )

      val output =
        KafkaAvroOutput(
          topic = Topic("person"),
          bootstrapServers = kafkaServers,
          AvroConfig(
            schemaRegistryUrl = getSchemaRegistryAddress,
            valueSchema = java.io.File("./outputs/src/test/resources/person-key.avsc").some, // Just for test
            autoRegisterSchemas = true
          )
        )

      val error = the[AvroException] thrownBy {
        runStream(kafkaServers, streams, builder, output)
      }
      error.getMessage should include("""Avro value encoder error: Missing INT node @ /id""")
    }

    it("Fail with broken schema (auto schema register enabled)") {
      val template = SourceTemplate(personTemplate)

      val streams = OutputStreamExecutor.make[IO]()

      val builder = TemplateBuilder.make(
        NonEmptyList.one(template),
        List(
          StringPatternGenerator(Variable("name"), NonEmptyString.unsafeFrom("username_###")),
          IntNumberGenerator(Variable("age"), min = 1.some, max = 50.some)
        )
      )

      val output =
        KafkaAvroOutput(
          topic = Topic("person"),
          bootstrapServers = kafkaServers,
          AvroConfig(
            schemaRegistryUrl = getSchemaRegistryAddress,
            keySchema = java.io.File("./outputs/src/test/resources/person-key.avsc").some,
            valueSchema = java.io.File("./outputs/src/test/resources/broken-person-value.avsc").some,
            autoRegisterSchemas = true
          )
        )

      val error = the[SchemaParseException] thrownBy {
        runStream(kafkaServers, streams, builder, output)
      }

      error.getMessage shouldBe """Schema parsing error: Record has no fields: {"type":"record","name":"PersonKey","namespace":"io.gen4s"}"""
    }

  }

}
