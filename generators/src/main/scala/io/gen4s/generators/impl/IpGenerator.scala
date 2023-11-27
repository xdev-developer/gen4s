package io.gen4s.generators.impl

import io.circe.derivation.ConfiguredCodec
import io.gen4s.core.generators.{*, given}
import io.gen4s.generators.codec.given
import io.gen4s.generators.render.{RandomIPv4, RandomIPv6}

final case class IpGenerator(variable: Variable, ipv6: Boolean = false) extends Generator derives ConfiguredCodec:

  override def gen(): GeneratedValue = GeneratedValue.fromString {
    if (ipv6) RandomIPv6.generate(0, 0) else RandomIPv4.generate(0, 0)
  }
