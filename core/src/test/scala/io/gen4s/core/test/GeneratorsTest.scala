package io.gen4s.core.test

import org.scalatest.funspec.AnyFunSpec
import org.scalatest.matchers.should.Matchers
import org.scalatest.EitherValues

import io.circe.parser.*
import io.circe.Decoder
import io.gen4s.core.generators.impl.TimestampGenerator
import io.gen4s.core.generators.Generator
import io.gen4s.core.generators.Generators
import io.gen4s.core.generators.Variable

class GeneratorsTest extends AnyFunSpec with Matchers with EitherValues {

  val testV = Variable("test")

  describe("Variable generators") {

    it("Timestamp generator") {
      val g = TimestampGenerator(testV)
      val r = g.gen().as[Long].value

      info(s"Generated result $r")

      testCodec[TimestampGenerator](
        s"""
          { 
            "variable": "${testV.name}",
             "type": "${Generators.TimeStamp.entryName}"}
             """,
        g
      )
    }

    def testCodec[T: Decoder](json: String, expected: T) = {
      decode[T](json).value shouldBe expected
      decode[Generator](json).value shouldBe expected
    }
  }
}
