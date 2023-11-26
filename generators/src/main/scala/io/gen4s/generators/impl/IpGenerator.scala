package io.gen4s.generators.impl

import io.circe.derivation.ConfiguredCodec
import io.gen4s.core.generators.{*, given}
import io.gen4s.generators.codec.given
import io.gen4s.generators.render.RandomIPv4

final case class IpGenerator(variable: Variable) extends Generator derives ConfiguredCodec:
  override def gen(): GeneratedValue = GeneratedValue.fromString(RandomIPv4.generate(0, 0))
