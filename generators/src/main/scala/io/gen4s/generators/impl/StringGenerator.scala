package io.gen4s
package generators
package impl

import scala.util.Random

import io.circe.derivation.ConfiguredCodec
import io.gen4s.core.generators.{*, given}

final case class StringGenerator(variable: Variable) extends Generator derives ConfiguredCodec:
  private val maxLen                 = 20
  override def gen(): GeneratedValue = GeneratedValue.fromString(Random.nextString(maxLen))
