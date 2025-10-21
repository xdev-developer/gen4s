package io.gen4s.generators.impl

import io.circe.Json
import io.circe.derivation.ConfiguredCodec
import io.gen4s.core.generators.*
import io.gen4s.generators.codec.given

final case class StaticValueGenerator(variable: Variable, value: Json) extends Generator derives ConfiguredCodec:
  override def gen(): GeneratedValue = GeneratedValue(value)
