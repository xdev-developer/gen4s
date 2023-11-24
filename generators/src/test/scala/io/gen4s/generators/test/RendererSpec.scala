package io.gen4s.generators.test

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike

import io.gen4s.generators.dsl.Dsl
import io.gen4s.generators.render.Renderer

import Renderer.given

class RendererSpec extends AnyWordSpecLike with Matchers:

  "Renderrer" should {

    "generate random values for all Dsl types" in {

      val tpl: Seq[Dsl] =
        Seq(Dsl.AnySymbols(2, 3), Dsl.Word(3, 10), Dsl.Number(3, 5), Dsl.HEX(3, 5), Dsl.IPv4, Dsl.IPv6, Dsl.MacAddress)

      tpl.map(t => gen(t))

      succeed
    }

    "generate AnySymbols with fixed length" in {
      gen(Dsl.AnySymbols(3, 3)).size shouldBe 3
    }

    "generate a random word of fixed length" in {
      gen(Dsl.Word(4, 4)).size shouldBe 4
    }

    "generate a random number between min and max bounds" in {
      val num = gen(Dsl.Number(5, 10)).toDouble

      num >= 5 && num <= 10 shouldBe true
    }

    "generate a random hex with fixed length" in {
      gen(Dsl.HEX(5, 5)).size shouldBe 5
    }

    "generate a random IPv4" in {
      gen(Dsl.IPv4) should fullyMatch regex """^(?:[0-9]{1,3}\.){3}[0-9]{1,3}$"""
    }

    "generate a random IPv6" in {
      gen(
        Dsl.IPv6
      ) should fullyMatch regex """(([0-9a-fA-F]{1,4}:){7,7}[0-9a-fA-F]{1,4}|([0-9a-fA-F]{1,4}:){1,7}:|([0-9a-fA-F]{1,4}:){1,6}:[0-9a-fA-F]{1,4}|([0-9a-fA-F]{1,4}:){1,5}(:[0-9a-fA-F]{1,4}){1,2}|([0-9a-fA-F]{1,4}:){1,4}(:[0-9a-fA-F]{1,4}){1,3}|([0-9a-fA-F]{1,4}:){1,3}(:[0-9a-fA-F]{1,4}){1,4}|([0-9a-fA-F]{1,4}:){1,2}(:[0-9a-fA-F]{1,4}){1,5}|[0-9a-fA-F]{1,4}:((:[0-9a-fA-F]{1,4}){1,6})|:((:[0-9a-fA-F]{1,4}){1,7}|:)|fe80:(:[0-9a-fA-F]{0,4}){0,4}%[0-9a-zA-Z]{1,}|::(ffff(:0{1,4}){0,1}:){0,1}((25[0-5]|(2[0-4]|1{0,1}[0-9]){0,1}[0-9])\.){3,3}(25[0-5]|(2[0-4]|1{0,1}[0-9]){0,1}[0-9])|([0-9a-fA-F]{1,4}:){1,4}:((25[0-5]|(2[0-4]|1{0,1}[0-9]){0,1}[0-9])\.){3,3}(25[0-5]|(2[0-4]|1{0,1}[0-9]){0,1}[0-9]))"""
    }

    "generate a random mac address" in {
      gen(Dsl.MacAddress) should fullyMatch regex "^([0-9A-Fa-f]{2}[:-]){5}([0-9A-Fa-f]{2})$"
    }
  }

  private def gen[A <: Dsl](obj: A)(using render: Renderer[A]): String =
    render.generate(obj)
