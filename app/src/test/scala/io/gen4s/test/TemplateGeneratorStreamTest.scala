package io.gen4s.test

import org.scalatest.funspec.AsyncFunSpec
import org.scalatest.matchers.should.Matchers

import cats.effect.testing.scalatest.AsyncIOSpec
import cats.effect.IO
import io.gen4s.core.generators.Variable
import io.gen4s.core.streams.GeneratorStream
import io.gen4s.core.templating.*
import io.gen4s.core.Domain.NumberOfSamplesToGenerate
import io.gen4s.generators.impl.TimestampGenerator

class TemplateGeneratorStreamTest extends AsyncFunSpec with AsyncIOSpec with Matchers {

  private val testV      = Variable("test")
  private val numSamples = NumberOfSamplesToGenerate(5)

  describe("Generator Stream") {

    it("Run simple generation stream") {
      val sourceTemplate = SourceTemplate("timestamp: {{test}}")
      val tsGenerator    = TimestampGenerator(testV)

      val tc = TemplateBuilder.make(
        sourceTemplates = List(sourceTemplate),
        generators = List(tsGenerator),
        globalVariables = List(testV),
        transformers = Set.empty[OutputTransformer]
      )

      val stream = GeneratorStream.stream[IO](numSamples, tc)

      stream
        .map(_.render())
        .compile
        .toList
        .asserting { elements =>
          elements.size shouldBe numSamples.value

          elements.foreach(c => info("Generated content: " + c.content))

          elements.head.content should fullyMatch regex "timestamp: ([0-9])+"
        }

    }

  }
}
