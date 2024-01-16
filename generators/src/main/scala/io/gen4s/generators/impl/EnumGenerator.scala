package io.gen4s.generators.impl

import scala.util.Random

import cats.data.NonEmptyList
import io.circe.derivation.ConfiguredCodec
import io.gen4s.core.generators.*
import io.gen4s.generators.codec.given

final case class EnumGenerator(variable: Variable, oneOf: NonEmptyList[String]) extends Generator
    derives ConfiguredCodec:

  private val values = oneOf.toList

  override def gen(): GeneratedValue = {
    GeneratedValue.fromString(values(Random.nextInt(values.length)))
  }
