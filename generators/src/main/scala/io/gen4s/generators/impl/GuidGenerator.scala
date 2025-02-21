package io.gen4s.generators.impl

import com.github.kolotaev.ride.Id

import io.circe.derivation.ConfiguredCodec
import io.gen4s.core.generators.*
import io.gen4s.generators.codec.given

final case class GuidGenerator(variable: Variable) extends Generator derives ConfiguredCodec:
  override def gen(): GeneratedValue = GeneratedValue.fromString(String.valueOf(Id()))
