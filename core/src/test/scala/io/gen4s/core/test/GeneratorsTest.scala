package io.gen4s.core.test

import org.scalatest.funspec.AnyFunSpec
import org.scalatest.matchers.must.Matchers
import org.scalatest.EitherValues

import io.gen4s.core.generators.impl.TimestampGenerator
import io.gen4s.core.generators.Variable

class GeneratorsTest extends AnyFunSpec with Matchers with EitherValues {

  val testV = Variable("test")

  describe("Variable generators") {

    it("Timestamp generator") {
      val g = TimestampGenerator(testV)
      val r = g.gen().as[Long].value

      info(s"Generated result $r")
    }
  }
}
