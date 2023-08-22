package io.gen4s.core
package generators
package impl

import scala.util.Random

final case class StringGenerator(variable: Variable) extends Generator derives ConfiguredCodec:
  private val maxLen                 = 20
  override def gen(): GeneratedValue = GeneratedValue.fromString(Random.nextString(maxLen))
