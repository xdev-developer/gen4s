package io.gen4s.core.generators.impl

import java.time.Instant

import io.gen4s.core.generators.*

final case class TimestampGenerator(variable: Variable) extends Generator {

  override def gen(): GeneratedValue = {
    GeneratedValue.fromLong(Instant.now().toEpochMilli())
  }
}
