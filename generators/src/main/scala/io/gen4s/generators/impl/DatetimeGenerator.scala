package io.gen4s
package generators
package impl

import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.time.LocalDateTime

import io.circe.derivation.ConfiguredCodec
import io.gen4s.core.generators.*
import io.gen4s.generators.codec.given

final case class DatetimeGenerator(
  variable: Variable,
  format: Option[String] = None,
  shiftDays: Option[Long] = None,
  shiftHours: Option[Long] = None,
  shiftMinutes: Option[Long] = None,
  shiftSeconds: Option[Long] = None)
    extends Generator derives ConfiguredCodec:

  private val defaultFormat: String = "MM/dd/yyyy"

  override def gen(): GeneratedValue = {
    val dt = LocalDateTime
      .now()
      .plus(shiftSeconds.getOrElse(0L), ChronoUnit.SECONDS)
      .plus(shiftDays.getOrElse(0L), ChronoUnit.DAYS)
      .plus(shiftHours.getOrElse(0L), ChronoUnit.HOURS)
      .plus(shiftMinutes.getOrElse(0L), ChronoUnit.MINUTES)

    val f = DateTimeFormatter.ofPattern(format.getOrElse(defaultFormat))
    GeneratedValue.fromString(dt.format(f))
  }
