package io.gen4s.core
package generators
package impl

import scala.util.Random

final case class IntGenerator(variable: Variable) extends Generator derives ConfiguredCodec:
  override def gen(): GeneratedValue = GeneratedValue.fromInt(Random.nextInt())
