package io.gen4s.core.test

import org.scalatest.funspec.AnyFunSpec
import org.scalatest.matchers.should.Matchers
import org.scalatest.EitherValues

import cats.implicits.*
import io.gen4s.core.generators.impl.TimestampGenerator
import io.gen4s.core.generators.Variable
import io.gen4s.core.templating.*

class TemplateGeneratorTest extends AnyFunSpec with Matchers with EitherValues {

  val testV = Variable("test")

  describe("Template generator") {

    it("Generate text template") {
      val sourceTemplate = SourceTemplate(s""""hello": $${test}""")
      val tsGenerator    = TimestampGenerator(testV)

      val generator = TemplateGenerator.make(
        sourceTemplates = List(sourceTemplate),
        generators = List(tsGenerator),
        globalVariables = List()
      )

      val result = generator.generate()
      result shouldBe List(
        TextTemplate(source = sourceTemplate, globalValues = Map.empty, generators = List(tsGenerator))
      )
    }

    it("Generate text template with global variables") {
      val sourceTemplate = SourceTemplate(s""""hello": $${test}""")
      val tsGenerator    = TimestampGenerator(testV)

      val generator = TemplateGenerator.make(
        sourceTemplates = List(sourceTemplate),
        generators = List(tsGenerator),
        globalVariables = List(testV)
      )

      val result = generator.generate()
      result should not be empty
      val head = result.head
      head shouldBe an[TextTemplate]
      val template = head.asInstanceOf[TextTemplate]
      template.source shouldBe sourceTemplate
      template.globalValues should not be empty
      template.generators shouldBe empty
    }

  }
}
