package io.gen4s.test

import org.scalatest.{EitherValues, OptionValues}
import org.scalatest.funspec.AnyFunSpec
import org.scalatest.matchers.should.Matchers

import cats.data.NonEmptyList
import cats.implicits.*
import io.gen4s.core.generators.{GeneratedValue, Variable}
import io.gen4s.core.templating.*
import io.gen4s.core.InputRecord
import io.gen4s.generators.impl.TimestampGenerator

class TemplateBuilderTest extends AnyFunSpec with Matchers with EitherValues with OptionValues {

  private val testV = Variable("test")
  private val nameV = Variable("name")

  describe("Template builder") {

    it("Build text template") {
      val sourceTemplate = SourceTemplate(""""hello": ${test}""")
      val tsGenerator    = TimestampGenerator(testV)

      val builder = TemplateBuilder.make(
        sourceTemplates = NonEmptyList.one(sourceTemplate),
        generators = List(tsGenerator)
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
      val sourceTemplate = SourceTemplate(""""hello": ${test}""")
      val tsGenerator    = TimestampGenerator(testV)

      val builder = TemplateBuilder.make(
        sourceTemplates = NonEmptyList.one(sourceTemplate),
        generators = List(tsGenerator),
        globalVariables = Set(testV)
      )

      val result = builder.build()
      result should not be empty
      val head = result.headOption.value
      head shouldBe an[TextTemplate]
      head match {
        case template: TextTemplate =>
          template.source shouldBe sourceTemplate
          template.context.globalValues should not be empty
          template.context.generators shouldBe empty
        case _ => fail()
      }
    }

    it("Build template from records stream") {
      val sourceTemplate = SourceTemplate("""{ "ts": ${test}, "username": "${name}" }""")
      val tsGenerator    = TimestampGenerator(testV)

      val builder = TemplateBuilder.ofRecordsStream(
        sourceTemplates = NonEmptyList.one(sourceTemplate),
        generators = List(tsGenerator),
        globalVariables = Set(testV),
        recordsStream = NonEmptyList
          .one(InputRecord.of(nameV, GeneratedValue.fromString("Den")))
      )

      val result = builder.build()
      result should not be empty
      val head = result.headOption.value
      head shouldBe an[TextTemplate]
      head match {
        case template: TextTemplate =>
          val rendered = template.render().asPrettyString
          info(rendered)

          rendered should include(""""username" : "Den"""")
          template.source shouldBe sourceTemplate
          template.context.globalValues should not be empty
          template.context.generators shouldBe empty
        case _ => fail()
      }
    }

    it("Build template with user input") {
      val sourceTemplate = SourceTemplate(""""hello": ${test}, "event_id":${id} """)
      val tsGenerator    = TimestampGenerator(testV)

      val builder = TemplateBuilder.make(
        sourceTemplates = NonEmptyList.one(sourceTemplate),
        generators = List(tsGenerator),
        userInput = Map(Variable("id") -> GeneratedValue.fromInt(12345))
      )

      val result = builder.build()
      result should not be empty
      val head = result.headOption.value
      head shouldBe an[TextTemplate]
      head match {
        case template: TextTemplate =>
          template.source shouldBe sourceTemplate
          template.context.globalValues should not be empty
          template.context.generators should not be empty
          val rendered = template.render().value
          info(rendered)
          rendered should include(""""event_id":12345""")
        case _ => fail()
      }
    }

    it("Build template from records stream and user input") {
      val sourceTemplate = SourceTemplate("""{ "ts": ${test}, "id": ${id}, "username": "${name}" }""")
      val tsGenerator    = TimestampGenerator(testV)

      val builder = TemplateBuilder.ofRecordsStream(
        sourceTemplates = NonEmptyList.one(sourceTemplate),
        generators = List(tsGenerator),
        globalVariables = Set(testV),
        userInput = Map(Variable("id") -> GeneratedValue.fromInt(12345)),
        recordsStream = NonEmptyList
          .one(InputRecord.of(nameV, GeneratedValue.fromString("Den")))
      )

      val result = builder.build()
      result should not be empty
      val head = result.headOption.value
      head shouldBe an[TextTemplate]
      head match {
        case template: TextTemplate =>
          val rendered = template.render().asPrettyString
          info(rendered)

          rendered should include(""""username" : "Den"""")
          rendered should include(""""id" : 12345""")
          template.source shouldBe sourceTemplate
          template.context.globalValues should not be empty
          template.context.generators shouldBe empty
        case _ => fail()
      }
    }

  }
}
