package io.gen4s.test.outputs

import org.scalatest.funspec.AsyncFunSpec
import org.scalatest.matchers.should.Matchers
import org.scalatest.OptionValues
import org.typelevel.log4cats.slf4j.Slf4jLogger
import org.typelevel.log4cats.Logger

import com.dimafeng.testcontainers.scalatest.TestContainersForAll
import com.dimafeng.testcontainers.KafkaContainer

import cats.data.NonEmptyList
import cats.effect.{IO, Sync}
import cats.effect.testing.scalatest.AsyncIOSpec
import io.gen4s.core.generators.Variable
import io.gen4s.core.streams.GeneratorStream
import io.gen4s.core.templating.{SourceTemplate, TemplateBuilder}
import io.gen4s.core.Domain.*
import io.gen4s.generators.impl.TimestampGenerator
import io.gen4s.outputs.KafkaOutput
import io.gen4s.outputs.OutputStreamExecutor

class KafkaOutputStreamTest
    extends AsyncFunSpec
    with AsyncIOSpec
    with TestContainersForAll
    with Matchers
    with KafkaConsumers
    with OptionValues {

  override type Containers = KafkaContainer

  override def startContainers(): Containers = {
    val kafka = KafkaContainer.Def().start()
    kafka
  }

  private val template = SourceTemplate("timestamp: ${ts}")

  given logger[F[_]: Sync]: Logger[F] = Slf4jLogger.getLogger[F]

  describe("Kafka output stream") {

    it("Send records to kafka topic") {
      withContainers { kafka =>
        val streams = OutputStreamExecutor.make[IO]()
        val builder = TemplateBuilder.make(
          NonEmptyList.one(template),
          List(TimestampGenerator(Variable("ts")))
        )

        val output = KafkaOutput(Topic("test-topic"), BootstrapServers(kafka.bootstrapServers))

        val n = NumberOfSamplesToGenerate(10)

        (for {
          _ <- streams.write(n, GeneratorStream.stream[IO](n, builder), output)
          r <- consumeAllAsMessages[IO, String](output.topic, output.bootstrapServers, count = n.value.toLong)
        } yield r).asserting { list =>
          list should not be empty
          list.size shouldBe n.value
          list.headOption.map(_.value).value should include("timestamp")
        }
      }
    }

    it("Send records with headers to kafka topic") {
      withContainers { kafka =>
        val streams = OutputStreamExecutor.make[IO]()
        val builder = TemplateBuilder.make(
          NonEmptyList.one(template),
          List(TimestampGenerator(Variable("ts")))
        )

        val output = KafkaOutput(Topic("test-topic"), BootstrapServers(kafka.bootstrapServers))

        val n = NumberOfSamplesToGenerate(10)

        (for {
          _ <- streams.write(n, GeneratorStream.stream[IO](n, builder), output)
          r <- consumeAllAsMessages[IO, String](output.topic, output.bootstrapServers, count = n.value.toLong)
        } yield r).asserting { list =>
          list should not be empty
          list.size shouldBe n.value
          list.headOption.map(_.value).value should include("timestamp")
          list.headOption.map(_.headers) shouldBe Symbol("defined")
        }
      }
    }

    it("Decode input as key-value message") {
      val kvTemplate = SourceTemplate("""{ "key": "my-key", "value": { "timestamp": ${ts} } }""")
      withContainers { kafka =>
        val streams = OutputStreamExecutor.make[IO]()
        val builder = TemplateBuilder.make(
          NonEmptyList.one(kvTemplate),
          List(TimestampGenerator(Variable("ts")))
        )

        val output =
          KafkaOutput(Topic("test-topic-kv"), BootstrapServers(kafka.bootstrapServers), decodeInputAsKeyValue = true)

        val n = NumberOfSamplesToGenerate(10)

        (for {
          _ <- streams.write(n, GeneratorStream.stream[IO](n, builder), output)
          r <- consumeAllAsMessages[IO, String](output.topic, output.bootstrapServers, count = n.value.toLong)
        } yield r).asserting { list =>
          list should not be empty
          list.headOption.flatMap(_.key).value shouldBe "my-key"
          list.headOption.map(_.value).value should include("timestamp")
          list.headOption.map(_.headers) shouldBe Symbol("defined")
        }
      }
    }

    it("Send tombstone records to kafka topic") {
      val kvTemplate = SourceTemplate("""{ "key": "my-key", "value": { "timestamp": ${ts} } }""")
      withContainers { kafka =>
        val streams = OutputStreamExecutor.make[IO]()
        val builder = TemplateBuilder.make(
          NonEmptyList.one(kvTemplate),
          List(TimestampGenerator(Variable("ts")))
        )

        val output =
          KafkaOutput(
            Topic("test-topic-kv"),
            BootstrapServers(kafka.bootstrapServers),
            decodeInputAsKeyValue = true,
            writeTombstoneRecord = true
          )

        val n = NumberOfSamplesToGenerate(10)

        (for {
          _ <- streams.write(n, GeneratorStream.stream[IO](n, builder), output)
        } yield true).assertNoException
      }
    }
  }

}
