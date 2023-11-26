package io.gen4s
package generators
package impl

import scala.util.Random

import io.circe.derivation.ConfiguredCodec
import io.gen4s.core.generators.{*, given}
import io.gen4s.generators.codec.given

final case class IntNumberGenerator(variable: Variable, min: Option[Int] = None, max: Option[Int] = None)
    extends Generator derives ConfiguredCodec:

  override def gen(): GeneratedValue =
    GeneratedValue.fromInt(min.getOrElse(0) + Random.nextInt((max.getOrElse(100) - min.getOrElse(0)) + 1))
