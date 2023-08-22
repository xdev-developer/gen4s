package io.gen4s.core
package generators
package impl

import java.time.Instant

final case class TimestampGenerator(variable: Variable) extends Generator derives ConfiguredCodec:

  override def gen(): GeneratedValue = GeneratedValue.fromLong(Instant.now().toEpochMilli())
