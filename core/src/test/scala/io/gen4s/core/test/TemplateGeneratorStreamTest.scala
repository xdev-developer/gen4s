package io.gen4s.core.test

import org.scalatest.funspec.AsyncFunSpec
import org.scalatest.matchers.should.Matchers

import cats.effect.testing.scalatest.AsyncIOSpec
import cats.effect.IO
import io.gen4s.core.generators.impl.TimestampGenerator
import io.gen4s.core.generators.Variable
import io.gen4s.core.streams.GeneratorStream
import io.gen4s.core.templating.*
import io.gen4s.core.Domain.NumberOfSamplesToGenerate

class TemplateGeneratorStreamTest extends AsyncFunSpec with AsyncIOSpec with Matchers {

  val testV              = Variable("test")
  private val numSamples = NumberOfSamplesToGenerate(5)

  describe("Generator Stream") {

    it("Run simple generation stream") {
      val sourceTemplate = SourceTemplate("timestamp: {{test}}")
      val tsGenerator    = TimestampGenerator(testV)

      val tc = TemplateBuilder.make(
        sourceTemplates = List(sourceTemplate),
        generators = List(tsGenerator),
        globalVariables = List(testV)
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
