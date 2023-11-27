package io.gen4s.generators.impl

import scala.math.BigDecimal.RoundingMode
import scala.util.Random

import io.circe.derivation.ConfiguredCodec
import io.gen4s.core.generators.{*, given}
import io.gen4s.generators.codec.given

final case class DoubleNumberGenerator(
  variable: Variable,
  min: Option[Double] = None,
  max: Option[Double] = None,
  scale: Option[Int] = None)
    extends Generator
    derives ConfiguredCodec:

  override def gen(): GeneratedValue = {
    val minV  = min.getOrElse(0.0d)
    val maxV  = max.getOrElse(100.0d)
    val value = BigDecimal(minV + Random.nextDouble() * (maxV - minV))
    GeneratedValue.fromDecimal(value.setScale(scale.getOrElse(2), RoundingMode.HALF_EVEN))
  }
