package io.gen4s.generators.test

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AsyncWordSpecLike

import cats.*
import cats.effect.*
import cats.effect.testing.scalatest.AsyncIOSpec
import io.gen4s.generators.TemplateGenerator

class TemplateGeneratorSpec extends AsyncWordSpecLike with AsyncIOSpec with Matchers:

  "TemplateGenerator" should {
    "parse template and generate string with random values" in {
      val template = "We need to send a message to ip %ip4 from %mac with IP v6 %ip6"

      TemplateGenerator.make[IO].process(template).map { res =>
        res should fullyMatch regex """We need to send a message to ip (?:[0-9]{1,3}\.){3}[0-9]{1,3} from ([0-9A-Fa-f]{2}[:-]){5}([0-9A-Fa-f]{2}) with IP v6 (([0-9a-fA-F]{1,4}:){7,7}[0-9a-fA-F]{1,4}|([0-9a-fA-F]{1,4}:){1,7}:|([0-9a-fA-F]{1,4}:){1,6}:[0-9a-fA-F]{1,4}|([0-9a-fA-F]{1,4}:){1,5}(:[0-9a-fA-F]{1,4}){1,2}|([0-9a-fA-F]{1,4}:){1,4}(:[0-9a-fA-F]{1,4}){1,3}|([0-9a-fA-F]{1,4}:){1,3}(:[0-9a-fA-F]{1,4}){1,4}|([0-9a-fA-F]{1,4}:){1,2}(:[0-9a-fA-F]{1,4}){1,5}|[0-9a-fA-F]{1,4}:((:[0-9a-fA-F]{1,4}){1,6})|:((:[0-9a-fA-F]{1,4}){1,7}|:)|fe80:(:[0-9a-fA-F]{0,4}){0,4}%[0-9a-zA-Z]{1,}|::(ffff(:0{1,4}){0,1}:){0,1}((25[0-5]|(2[0-4]|1{0,1}[0-9]){0,1}[0-9])\.){3,3}(25[0-5]|(2[0-4]|1{0,1}[0-9]){0,1}[0-9])|([0-9a-fA-F]{1,4}:){1,4}:((25[0-5]|(2[0-4]|1{0,1}[0-9]){0,1}[0-9])\.){3,3}(25[0-5]|(2[0-4]|1{0,1}[0-9]){0,1}[0-9]))"""
      }
    }
  }
