package io.gen4s.generators.impl

import io.circe.derivation.ConfiguredCodec
import io.gen4s.core.generators.{*, given}
import io.gen4s.generators.codec.given

final case class ListGenerator(variable: Variable, generator: Generator, len: Option[Int] = None) extends Generator
    derives ConfiguredCodec:

  override def gen(): GeneratedValue = {
    GeneratedValue.fromSeq {
      Vector.fill(len.getOrElse(1))(generator.gen().v)
    }
  }
