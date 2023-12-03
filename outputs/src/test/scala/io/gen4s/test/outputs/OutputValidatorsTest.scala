package io.gen4s.test.outputs

import org.scalatest.freespec.AsyncFreeSpec
import org.scalatest.matchers.should.Matchers

import cats.effect.testing.scalatest.AsyncIOSpec
import io.gen4s.core.templating.{OutputValidator, RenderedTemplate}

class OutputValidatorsTest extends AsyncFreeSpec with AsyncIOSpec with Matchers {

  "Output Validator Json" - {

    "return valid for json template" in {
      OutputValidator.JSON
        .validate(RenderedTemplate("""{"key":"value"}"""))
        .isValid shouldBe true
    }

    "return error for invalid json template" in {
      OutputValidator.JSON
        .validate(RenderedTemplate("""{"key":value}"""))
        .isValid shouldBe false

    }

  }

  "Output Validator Vars" - {

    "return valid for json template" in {
      OutputValidator.MissingVars
        .validate(RenderedTemplate("""{"key":"value"}"""))
        .isValid shouldBe true
    }

    "return error for invalid json template" in {
      OutputValidator.MissingVars
        .validate(RenderedTemplate("""{"key": {{variable}}}"""))
        .isValid shouldBe false

    }

  }

}
