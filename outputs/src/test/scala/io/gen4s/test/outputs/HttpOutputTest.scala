package io.gen4s.test.outputs

import org.scalatest.funspec.AsyncFunSpec
import org.scalatest.matchers.should.Matchers

import cats.data.NonEmptyList
import cats.effect.testing.scalatest.AsyncIOSpec
import cats.effect.IO
import io.gen4s.core.generators.Variable
import io.gen4s.core.streams.GeneratorStream
import io.gen4s.core.templating.{OutputTransformer, SourceTemplate, TemplateBuilder}
import io.gen4s.core.Domain.NumberOfSamplesToGenerate
import io.gen4s.generators.impl.TimestampGenerator
import io.gen4s.outputs.{HttpMethods, HttpOutput, OutputStreamExecutor}

class HttpOutputTest extends AsyncFunSpec with AsyncIOSpec with Matchers {

  private val template = SourceTemplate("{ timestamp: {{ts}} }")

  describe("Http Output") {

    it("Send POST request") {
      val streams = OutputStreamExecutor.make[IO]()
      val builder = TemplateBuilder.make(
        NonEmptyList.one(template),
        List(TimestampGenerator(Variable("ts"))),
        Set.empty[Variable],
        Set.empty[OutputTransformer]
      )

      val output = HttpOutput(
        "https://postman-echo.com/post",
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
        List(TimestampGenerator(Variable("ts"))),
        Set.empty[Variable],
        Set.empty[OutputTransformer]
      )

      val output = HttpOutput(
        "https://postman-echo.com/put",
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
        List(TimestampGenerator(Variable("ts"))),
        Set.empty[Variable],
        Set.empty[OutputTransformer]
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
