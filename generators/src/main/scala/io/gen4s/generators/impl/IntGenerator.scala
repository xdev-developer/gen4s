package io.gen4s
package generators
package impl

import scala.util.Random

import io.circe.derivation.ConfiguredCodec
import io.gen4s.core.generators.{*, given}

final case class IntGenerator(variable: Variable) extends Generator derives ConfiguredCodec:
  override def gen(): GeneratedValue = GeneratedValue.fromInt(Random.nextInt())
