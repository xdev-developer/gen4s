package io.gen4s.generators.impl

import scala.util.Random

import io.circe.derivation.ConfiguredCodec
import io.gen4s.core.generators.{*, given}
import io.gen4s.generators.codec.given

final case class DoubleNumberGenerator(variable: Variable, min: Option[Double] = None, max: Option[Double] = None)
    extends Generator derives ConfiguredCodec:

  override def gen(): GeneratedValue = {
    val minV = min.getOrElse(0.0d)
    val maxV = max.getOrElse(100.0d)
    GeneratedValue.fromDecimal(minV + Random.nextDouble() * (maxV - minV))
  }
