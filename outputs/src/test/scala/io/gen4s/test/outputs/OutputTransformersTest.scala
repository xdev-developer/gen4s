package io.gen4s.test.outputs

import org.scalatest.freespec.AsyncFreeSpec
import org.scalatest.matchers.should.Matchers

import cats.effect.testing.scalatest.AsyncIOSpec
import io.gen4s.core.templating.{OutputTransformer, RenderedTemplate}

class OutputTransformersTest extends AsyncFreeSpec with AsyncIOSpec with Matchers {

  "Output Transformers" - {

    "Json Minify transformer" in {
      OutputTransformer.JsonMinify
        .transform(RenderedTemplate("""{
                                      | "key": "value"
                                      |}""".stripMargin)) shouldBe RenderedTemplate("""{"key":"value"}""")
    }

    "Json Prettify transformer" in {
      OutputTransformer.JsonPrettify
        .transform(RenderedTemplate("""{"key":"value"}""")) shouldBe RenderedTemplate("""{
                                                                                        |  "key" : "value"
                                                                                        |}""".stripMargin)

    }

  }

}
