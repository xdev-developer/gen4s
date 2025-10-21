package io.gen4s.test.outputs

import org.scalatest.funspec.AsyncFunSpec
import org.scalatest.matchers.should.Matchers
import org.typelevel.log4cats.Logger
import org.typelevel.log4cats.slf4j.Slf4jLogger

import cats.data.NonEmptyList
import cats.effect.testing.scalatest.AsyncIOSpec
import cats.effect.{IO, Sync}
import io.gen4s.core.Domain.NumberOfSamplesToGenerate
import io.gen4s.core.generators.Variable
import io.gen4s.core.streams.GeneratorStream
import io.gen4s.core.templating.{SourceTemplate, TemplateBuilder}
import io.gen4s.generators.impl.TimestampGenerator
import io.gen4s.outputs.{HttpMethods, HttpOutput, OutputStreamExecutor}

class HttpOutputTest extends AsyncFunSpec with AsyncIOSpec with Matchers {

  private val template = SourceTemplate("{ timestamp: {{ts}} }")

  given logger[F[_]: Sync]: Logger[F] = Slf4jLogger.getLogger[F]

  describe("Http Output") {

    it("Send POST request") {
      val streams = OutputStreamExecutor.make[IO]()
      val builder = TemplateBuilder.make(
        NonEmptyList.one(template),
        List(TimestampGenerator(Variable("ts")))
      )

      val output = HttpOutput(
        "https://httpbingo.org/post",
        HttpMethods.Post
      )

      val n = NumberOfSamplesToGenerate(1)
      streams
        .write(n, GeneratorStream.stream[IO](n, builder), output)
        .assertNoException
    }

    it("Send PUT request") {
      val streams = OutputStreamExecutor.make[IO]()
      val builder = TemplateBuilder.make(
        NonEmptyList.one(template),
        List(TimestampGenerator(Variable("ts")))
      )

      val output = HttpOutput(
        "https://httpbingo.org/put",
        HttpMethods.Put
      )

      val n = NumberOfSamplesToGenerate(1)
      streams
        .write(n, GeneratorStream.stream[IO](n, builder), output)
        .assertNoException
    }

    it("Stop on failure") {
      val streams = OutputStreamExecutor.make[IO]()
      val builder = TemplateBuilder.make(
        NonEmptyList.one(template),
        List(TimestampGenerator(Variable("ts")))
      )

      val output = HttpOutput(
        "https://example.com/404",
        HttpMethods.Put
      )

      val n = NumberOfSamplesToGenerate(1)
      streams
        .write(n, GeneratorStream.stream[IO](n, builder), output)
        .assertThrows[Exception]
    }
  }

}
