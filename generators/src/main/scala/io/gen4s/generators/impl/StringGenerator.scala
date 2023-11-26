package io.gen4s
package generators
package impl

import io.circe.derivation.ConfiguredCodec
import io.gen4s.core.generators.{*, given}
import io.gen4s.generators.codec.given
import io.gen4s.generators.render.RandomWord

final case class StringGenerator(variable: Variable, len: Option[Int] = None) extends Generator derives ConfiguredCodec:

  override def gen(): GeneratedValue = {
    val chars = len.getOrElse(10)
    GeneratedValue.fromString(RandomWord.generate(chars, chars))
  }
