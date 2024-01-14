package io.gen4s.generators.impl

import io.circe.derivation.ConfiguredCodec
import io.gen4s.core.generators.*
import io.gen4s.generators.codec.given
import io.gen4s.generators.render.RandomMacAddress

final case class MacAddressGenerator(variable: Variable) extends Generator derives ConfiguredCodec:

  override def gen(): GeneratedValue = {
    GeneratedValue.fromString(RandomMacAddress.generate(0, 0))
  }
