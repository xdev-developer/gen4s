package io.gen4s.generators.test

import org.scalatest.funspec.AnyFunSpec
import org.scalatest.matchers.should.Matchers
import org.scalatest.EitherValues

import scala.annotation.nowarn

import cats.data.NonEmptyList
import io.circe.{Decoder, Json}
import io.circe.parser.*
import io.gen4s.core.generators.{Generator, Variable}
import io.gen4s.generators.codec.given
import io.gen4s.generators.impl.*
import io.gen4s.generators.Generators

import eu.timepit.refined.types.string.NonEmptyString

class GeneratorsTest extends AnyFunSpec with Matchers with EitherValues {

  private val testV = Variable("test")

  describe("Variable generators") {

    it("Timestamp generator") {
      val g = TimestampGenerator(testV)
      val r = g.gen().as[Long].value

      info(s"Generated result $r")

      testCodec[TimestampGenerator](
        s""" { "variable": "${testV.value}", "type": "${Generators.TimeStamp.entryName}"}""",
        g
      )
    }

    it("Boolean generator") {
      val g = BooleanGenerator(testV)
      val r = g.gen().as[Boolean].value

      info(s"Generated result $r")

      testCodec[BooleanGenerator](
        s""" { "variable": "${testV.value}", "type": "${Generators.Boolean.entryName}"}""",
        g
      )
    }

    it("Int generator") {
      val g = IntNumberGenerator(testV)
      val r = g.gen().as[Int].value

      info(s"Generated result $r")

      testCodec[IntNumberGenerator](
        s""" { "variable": "${testV.value}", "type": "${Generators.Integer.entryName}"}""",
        g
      )
    }

    it("Double generator") {
      val g = DoubleNumberGenerator(testV)
      val r = g.gen().as[Double].value

      info(s"Generated result: $r")

      testCodec[DoubleNumberGenerator](
        s""" { "variable": "${testV.value}", "type": "${Generators.Double.entryName}"}""",
        g
      )
    }

    it("DateTime generator") {
      val g = DatetimeGenerator(testV)
      val r = g.gen().as[String].value

      info(s"Generated result: $r")

      testCodec[DatetimeGenerator](
        s""" { "variable": "${testV.value}", "type": "${Generators.Date.entryName}"}""",
        g
      )
    }

    it("String generator") {
      val g = StringGenerator(testV)
      val r = g.gen().as[String].value

      info(s"Generated result: $r")

      testCodec[StringGenerator](
        s""" { "variable": "${testV.value}", "type": "${Generators.String.entryName}"}""",
        g
      )
    }

    it("UUID generator") {
      val g = UuidGenerator(testV)
      val r = g.gen().as[String].value

      info(s"Generated result: $r")

      testCodec[UuidGenerator](
        s""" { "variable": "${testV.value}", "type": "${Generators.UUID.entryName}"}""",
        g
      )
    }

    it("GUID generator") {
      val g = GuidGenerator(testV)
      val r = g.gen().as[String].value

      info(s"Generated result: $r")

      testCodec[GuidGenerator](
        s""" { "variable": "${testV.value}", "type": "${Generators.GUID.entryName}"}""",
        g
      )
    }

    it("Static value generator") {
      val g = StaticValueGenerator(testV, Json.fromString("hello"))
      val r = g.gen().as[String].value

      info(s"Generated result: $r")

      testCodec[StaticValueGenerator](
        s""" { "variable": "${testV.value}", "type": "${Generators.Static.entryName}", "value": "hello"}""",
        g
      )
    }

    it("IP generator") {
      val g = IpGenerator(testV)
      val r = g.gen().as[String].value

      info(s"Generated result: $r")

      testCodec[IpGenerator](
        s""" { "variable": "${testV.value}", "type": "${Generators.Ip.entryName}"}""",
        g
      )
    }

    it("Env var generator") {
      val g = EnvVarGenerator(testV, NonEmptyString.unsafeFrom("os.value"), default = None)
      val r = g.gen().as[String].value

      info(s"Generated result: $r")

      testCodec[EnvVarGenerator](
        s""" { "variable": "${testV.value}", "type": "${Generators.EnvVar.entryName}", "name": "os.value"}""",
        g
      )
    }

    it("Env var generator - use var name when env var isn't found") {
      val g = EnvVarGenerator(testV, NonEmptyString.unsafeFrom("DUMMY"), default = None)
      val r = g.gen().as[String].value

      info(s"Generated result: $r")
      r shouldBe "DUMMY"
    }

    it("Env var generator - support default value") {
      val g = EnvVarGenerator(testV, NonEmptyString.unsafeFrom("DUMMY"), default = Some("default value"))
      val r = g.gen().as[String].value

      info(s"Generated result: $r")
      r shouldBe "default value"
    }

    it("String pattern generator") {
      val g = StringPatternGenerator(testV, NonEmptyString.unsafeFrom("hello-???-###"))
      val r = g.gen().as[String].value

      info(s"Generated result: $r")

      testCodec[StringPatternGenerator](
        s""" { "variable": "${testV.value}", "type": "${Generators.Pattern.entryName}", "pattern": "hello-???-###"}""",
        g
      )
    }

    it("Enum generator") {
      val g = EnumGenerator(testV, NonEmptyList.of("foo"))
      val r = g.gen().as[String].value

      info(s"Generated result: $r")

      testCodec[EnumGenerator](
        s""" { "variable": "${testV.value}", "type": "${Generators.Enum.entryName}", "oneOf": ["foo"]}""",
        g
      )
    }

    it("MAC Address generator") {
      val g = MacAddressGenerator(testV)
      val r = g.gen().as[String].value

      info(s"Generated result: $r")

      testCodec[MacAddressGenerator](
        s""" { "variable": "${testV.value}", "type": "${Generators.Mac.entryName}"}""",
        g
      )
    }

    @nowarn
    def testCodec[T: Decoder](json: String, expected: T) = {
      decode[T](json).value shouldBe expected
      decode[Generator](json).value shouldBe expected
    }
  }
}
