package io.gen4s
package generators
package impl

import java.time.Instant

import io.circe.derivation.ConfiguredCodec
import io.gen4s.core.generators.{*, given}

final case class TimestampGenerator(variable: Variable) extends Generator derives ConfiguredCodec:
  override def gen(): GeneratedValue = GeneratedValue.fromLong(Instant.now().toEpochMilli())
