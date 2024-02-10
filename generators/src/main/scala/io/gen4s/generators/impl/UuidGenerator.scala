package io.gen4s.generators.impl

import java.util.UUID

import io.circe.derivation.ConfiguredCodec
import io.gen4s.core.generators.*
import io.gen4s.generators.codec.given

final case class UuidGenerator(variable: Variable) extends Generator derives ConfiguredCodec:
  override def gen(): GeneratedValue = GeneratedValue.fromString(String.valueOf(UUID.randomUUID()))
