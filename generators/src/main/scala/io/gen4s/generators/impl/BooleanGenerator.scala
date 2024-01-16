package io.gen4s
package generators
package impl

import scala.util.Random

import io.circe.derivation.ConfiguredCodec
import io.gen4s.core.generators.*
import io.gen4s.generators.codec.given

final case class BooleanGenerator(variable: Variable) extends Generator derives ConfiguredCodec:
  override def gen(): GeneratedValue = GeneratedValue.fromBoolean(Random.nextBoolean())
