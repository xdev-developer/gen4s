package io.gen4s.test

import org.scalatest.funspec.AnyFunSpec
import org.scalatest.matchers.should.Matchers
import org.scalatest.EitherValues

import cats.data.NonEmptyList
import cats.implicits.*
import io.circe.Json
import io.gen4s.core.generators.Variable
import io.gen4s.core.templating.*
import io.gen4s.core.InputRecord
import io.gen4s.generators.impl.{StaticValueGenerator, TimestampGenerator}

class TemplateBuilderTest extends AnyFunSpec with Matchers with EitherValues {

  private val testV = Variable("test")
  private val nameV = Variable("name")

  describe("Template builder") {

    it("Build text template") {
      val sourceTemplate = SourceTemplate(s""""hello": {{test}}""")
      val tsGenerator    = TimestampGenerator(testV)

      val builder = TemplateBuilder.make(
        sourceTemplates = NonEmptyList.one(sourceTemplate),
        generators = List(tsGenerator),
        globalVariables = List(),
        transformers = Set.empty[OutputTransformer]
      )

      val result = builder.build()
      result shouldBe List(
        TextTemplate(
          source = sourceTemplate,
          context = TemplateContext(Map.empty, List(tsGenerator)),
          transformers = Set.empty[OutputTransformer]
        )
      )
    }

    it("Build template with global variables") {
      val sourceTemplate = SourceTemplate(s""""hello": {{test}}""")
      val tsGenerator    = TimestampGenerator(testV)

      val builder = TemplateBuilder.make(
        sourceTemplates = NonEmptyList.one(sourceTemplate),
        generators = List(tsGenerator),
        globalVariables = List(testV),
        transformers = Set.empty[OutputTransformer]
      )

      val result = builder.build()
      result should not be empty
      val head = result.head
      head shouldBe an[TextTemplate]
      val template = head.asInstanceOf[TextTemplate]
      template.source shouldBe sourceTemplate
      template.context.globalValues should not be empty
      template.context.generators shouldBe empty
    }

    it("Build template from records stream") {
      val sourceTemplate = SourceTemplate(s"""{ "ts": {{test}}, "username": "{{name}}" }""")
      val tsGenerator    = TimestampGenerator(testV)

      val builder = TemplateBuilder.ofRecordsStream(
        sourceTemplates = NonEmptyList.one(sourceTemplate),
        generators = List(tsGenerator),
        globalVariables = List(testV),
        recordsStream = NonEmptyList
          .one(InputRecord.of(nameV, StaticValueGenerator(nameV, Json.fromString("Den")).gen())),
        transformers = Set.empty[OutputTransformer]
      )

      val result = builder.build()
      result should not be empty
      val head = result.head
      head shouldBe an[TextTemplate]
      val template = head.asInstanceOf[TextTemplate]
      val rendered = template.render().asPrettyString
      info(rendered)

      rendered should include(""""username" : "Den"""")
      template.source shouldBe sourceTemplate
      template.context.globalValues should not be empty
      template.context.generators shouldBe empty

    }

  }
}
