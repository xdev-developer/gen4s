package io.gen4s.test.outputs

import org.scalatest.funspec.AsyncFunSpec
import org.scalatest.matchers.should.Matchers

import com.dimafeng.testcontainers.scalatest.TestContainersForAll
import com.dimafeng.testcontainers.KafkaContainer

import cats.effect.testing.scalatest.AsyncIOSpec
import cats.effect.IO
import io.gen4s.core.generators.impl.TimestampGenerator
import io.gen4s.core.generators.Variable
import io.gen4s.core.streams.GeneratorStream
import io.gen4s.core.templating.SourceTemplate
import io.gen4s.core.templating.TemplateBuilder
import io.gen4s.core.Domain.*
import io.gen4s.outputs.KafkaOutput
import io.gen4s.outputs.OutputStreamExecutor

class KafkaOutputStreamTest
    extends AsyncFunSpec
    with AsyncIOSpec
    with TestContainersForAll
    with Matchers
    with KafkaConsumers {

  override type Containers = KafkaContainer

  override def startContainers(): Containers = {
    val kafka = KafkaContainer.Def().start()
    kafka
  }

  private val template = SourceTemplate("timestamp: {{ts}}")

  describe("Kafka output stream") {

    it("Send records to kafka topic") {
      withContainers { kafka =>
        val streams = OutputStreamExecutor.make[IO]()
        val builder = TemplateBuilder.make(
          List(template),
          List(TimestampGenerator(Variable("ts"))),
          Nil
        )

        val output = KafkaOutput(Topic("test-topic"), BootstrapServers(kafka.bootstrapServers))

        val n = NumberOfSamplesToGenerate(10)

        (for {
          _ <- streams.write(n, GeneratorStream.stream[IO](n, builder), output)
          r <- consumeAllAsMessages[IO](output.topic, output.bootstrapServers, count = n.value.toLong)
        } yield r).asserting { list =>
          list should not be empty
          list.size shouldBe n.value
          list.head.value should include("timestamp")
        }
      }
    }

    it("Send records with headers to kafka topic") {
      withContainers { kafka =>
        val streams = OutputStreamExecutor.make[IO]()
        val builder = TemplateBuilder.make(
          List(template),
          List(TimestampGenerator(Variable("ts"))),
          Nil
        )

        val output = KafkaOutput(Topic("test-topic"), BootstrapServers(kafka.bootstrapServers))

        val n = NumberOfSamplesToGenerate(10)

        (for {
          _ <- streams.write(n, GeneratorStream.stream[IO](n, builder), output)
          r <- consumeAllAsMessages[IO](output.topic, output.bootstrapServers, count = n.value.toLong)
        } yield r).asserting { list =>
          list should not be empty
          list.size shouldBe n.value
          list.head.value should include("timestamp")
          list.head.headers shouldBe Symbol("defined")
        }
      }
    }
  }

}
