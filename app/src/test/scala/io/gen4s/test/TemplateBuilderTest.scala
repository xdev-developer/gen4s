package io.gen4s.test

import org.scalatest.funspec.AnyFunSpec
import org.scalatest.matchers.should.Matchers
import org.scalatest.EitherValues

import cats.implicits.*
import io.gen4s.core.generators.Variable
import io.gen4s.core.templating.*
import io.gen4s.generators.impl.TimestampGenerator

class TemplateBuilderTest extends AnyFunSpec with Matchers with EitherValues {

  private val testV = Variable("test")

  describe("Template builder") {

    it("Build text template") {
      val sourceTemplate = SourceTemplate(s""""hello": {{test}}""")
      val tsGenerator    = TimestampGenerator(testV)

      val builder = TemplateBuilder.make(
        sourceTemplates = List(sourceTemplate),
        generators = List(tsGenerator),
        globalVariables = List()
      )

      val result = builder.build()
      result shouldBe List(
        TextTemplate(source = sourceTemplate, globalValues = Map.empty, generators = List(tsGenerator))
      )
    }

    it("Build template with global variables") {
      val sourceTemplate = SourceTemplate(s""""hello": {{test}}""")
      val tsGenerator    = TimestampGenerator(testV)

      val builder = TemplateBuilder.make(
        sourceTemplates = List(sourceTemplate),
        generators = List(tsGenerator),
        globalVariables = List(testV)
      )

      val result = builder.build()
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
