package io.gen4s.test

import org.scalatest.funspec.AsyncFunSpec
import org.scalatest.matchers.should.Matchers
import org.scalatest.OptionValues

import cats.data.NonEmptyList
import cats.effect.testing.scalatest.AsyncIOSpec
import cats.effect.IO
import io.gen4s.core.generators.Variable
import io.gen4s.core.streams.GeneratorStream
import io.gen4s.core.templating.*
import io.gen4s.core.Domain.NumberOfSamplesToGenerate
import io.gen4s.generators.impl.{StringPatternGenerator, TimestampGenerator}

import eu.timepit.refined.types.string.NonEmptyString

class TemplateGeneratorStreamTest extends AsyncFunSpec with AsyncIOSpec with Matchers with OptionValues {

  private val timestampV = Variable("ts")
  private val nameV      = Variable("name")
  private val numSamples = NumberOfSamplesToGenerate(5)

  describe("Generator Stream") {

    it("Run simple generation stream") {
      val sourceTemplate = SourceTemplate("timestamp: ${ts}")
      val tsGenerator    = TimestampGenerator(timestampV)

      val tc = TemplateBuilder.make(
        sourceTemplates = NonEmptyList.one(sourceTemplate),
        generators = List(tsGenerator),
        globalVariables = Set.empty[Variable],
        transformers = Set.empty[OutputTransformer]
      )

      val stream = GeneratorStream.stream[IO](numSamples, tc)

      stream
        .map(_.render())
        .compile
        .toList
        .asserting { elements =>
          elements.size shouldBe numSamples.value

          elements.foreach(c => info("Generated content: " + c.asString))

          elements.headOption.map(_.asString).value should fullyMatch regex "timestamp: ([0-9])+"
        }

    }

    it("Support global variables") {
      val sourceTemplate    = SourceTemplate("username: ${name}")
      val usernameGenerator = StringPatternGenerator(nameV, pattern = NonEmptyString.unsafeFrom("user-###"))

      val tc = TemplateBuilder.make(
        sourceTemplates = NonEmptyList.one(sourceTemplate),
        generators = List(usernameGenerator),
        globalVariables = Set(nameV),
        transformers = Set.empty[OutputTransformer]
      )

      val stream = GeneratorStream.stream[IO](numSamples, tc)

      stream
        .map(_.render())
        .compile
        .toList
        .asserting { elements =>
          elements.foreach(c => info("Generated content: " + c.asString))
          elements.headOption.map(_.asString).value should fullyMatch regex "username: user-([0-9])+"
          elements.headOption.map(_.asString) shouldBe elements.lastOption.map(_.asString)
        }

    }

  }
}
