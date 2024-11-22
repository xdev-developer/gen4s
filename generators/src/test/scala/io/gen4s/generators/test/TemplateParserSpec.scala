package io.gen4s.generators.test

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike

import scala.annotation.nowarn

import io.gen4s.generators.dsl.Dsl
import io.gen4s.generators.dsl.TemplateParser

@nowarn
class TemplateParserSpec extends AnyWordSpecLike with Matchers:

  "TemplateParser" should {
    "parse any symbol successfully" in {
      TemplateParser.process("*") shouldBe Seq(Dsl.AnySymbols(1, 1))
    }

    "parse sequence of any symbols" in {
      TemplateParser.process("*{2}") shouldBe Seq(Dsl.AnySymbols(2, 2))
      an[Exception] should be thrownBy TemplateParser.process("*{5, 2}")
      TemplateParser.process("*{1, 6}") shouldBe Seq(Dsl.AnySymbols(1, 6))
    }

    "parse 2 words" in {
      TemplateParser.process("* *{2}") shouldBe Seq(Dsl.AnySymbols(1, 1), Dsl.PureValue(" "), Dsl.AnySymbols(2, 2))
    }

    "parse word" in {
      TemplateParser.process("%s{2, 5}") shouldBe Seq(Dsl.Word(2, 5))
    }

    "parse number" in {
      TemplateParser.process("%n{0, 50}") shouldBe Seq(Dsl.Number(0, 50))
    }

    "parse hex number" in {
      TemplateParser.process("#{2, 5}") shouldBe Seq(Dsl.HEX(2, 5))
    }

    "parse ip template" in {
      TemplateParser.process("%ip4") shouldBe Seq(Dsl.IPv4)
      TemplateParser.process("%ip6") shouldBe Seq(Dsl.IPv6)
      TemplateParser.process("%ip4 %ip6") shouldBe Seq(Dsl.IPv4, Dsl.PureValue(" "), Dsl.IPv6)
    }

    "parse mac address template" in {
      TemplateParser.process("%mac") shouldBe Seq(Dsl.MacAddress)
    }

    "parse template values together with pure ones" in {
      val template = "We need to send a message '*{2,5}' to ip %ip4 from %mac with %ip6"

      TemplateParser.process(template) should contain theSameElementsAs List(
        Dsl.PureValue("We"),
        Dsl.PureValue(" "),
        Dsl.PureValue("need"),
        Dsl.PureValue(" "),
        Dsl.PureValue("to"),
        Dsl.PureValue(" "),
        Dsl.PureValue("send"),
        Dsl.PureValue(" "),
        Dsl.PureValue("a"),
        Dsl.PureValue(" "),
        Dsl.PureValue("message"),
        Dsl.PureValue(" "),
        Dsl.PureValue("'"),
        Dsl.AnySymbols(2, 5),
        Dsl.PureValue("'"),
        Dsl.PureValue(" "),
        Dsl.PureValue("to"),
        Dsl.PureValue(" "),
        Dsl.PureValue("ip"),
        Dsl.PureValue(" "),
        Dsl.IPv4,
        Dsl.PureValue(" "),
        Dsl.PureValue("from"),
        Dsl.PureValue(" "),
        Dsl.MacAddress,
        Dsl.PureValue(" "),
        Dsl.PureValue("with"),
        Dsl.PureValue(" "),
        Dsl.IPv6
      )

    }
  }
