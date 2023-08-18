package io.gen4s.core
package generators
package impl

import java.time.temporal.{ChronoUnit, TemporalUnit}
import java.time.Instant

import scala.util.Random

final case class DatetimeGenerator(variable: Variable) extends Generator derives ConfiguredCodec:

  private val units     = Array(ChronoUnit.DAYS, ChronoUnit.HOURS, ChronoUnit.MINUTES, ChronoUnit.SECONDS)
  private val maxOffset = 100

  private def randomUnit: TemporalUnit = units(Random.nextInt(units.size))

  override def gen(): GeneratedValue =
    GeneratedValue.fromLong(Instant.now().minus(Random.nextInt(maxOffset), randomUnit).toEpochMilli())
