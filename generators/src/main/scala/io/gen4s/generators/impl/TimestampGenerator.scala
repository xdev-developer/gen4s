package io.gen4s
package generators
package impl

import java.time.temporal.ChronoUnit
import java.time.Instant
import java.util.concurrent.TimeUnit

import io.circe.derivation.ConfiguredCodec
import io.gen4s.core.generators.{*, given}
import io.gen4s.generators.codec.given

import enumeratum.{CirceEnum, EnumEntry}

final case class TimestampGenerator(
  variable: Variable,
  unit: Option[TSUnit] = None,
  shiftDays: Option[Long] = None,
  shiftHours: Option[Long] = None,
  shiftMinutes: Option[Long] = None,
  shiftSeconds: Option[Long] = None,
  shiftMillis: Option[Long] = None)
    extends Generator
    derives ConfiguredCodec {

  override def gen(): GeneratedValue = {
    val millis =
      Instant
        .now()
        .plus(shiftMillis.getOrElse(0L), ChronoUnit.MILLIS)
        .plus(shiftSeconds.getOrElse(0L), ChronoUnit.SECONDS)
        .plus(shiftDays.getOrElse(0L), ChronoUnit.DAYS)
        .plus(shiftHours.getOrElse(0L), ChronoUnit.HOURS)
        .plus(shiftMinutes.getOrElse(0L), ChronoUnit.MINUTES)
        .toEpochMilli

    GeneratedValue.fromLong(TsUnitConverter.convertFromMilliseconds(unit, millis))
  }
}

private object TsUnitConverter {

  def convertFromMilliseconds(toUnit: Option[TSUnit], millis: Long): Long =
    toUnit match {
      case Some(TSUnit.nanos)   => TimeUnit.MILLISECONDS.toNanos(millis)
      case Some(TSUnit.seconds) => TimeUnit.MILLISECONDS.toSeconds(millis)
      case Some(TSUnit.micros)  => TimeUnit.MILLISECONDS.toMicros(millis)
      case _                    => millis
    }
}

sealed abstract class TSUnit(override val entryName: String) extends EnumEntry

object TSUnit extends enumeratum.Enum[TSUnit] with CirceEnum[TSUnit] {
  val values: IndexedSeq[TSUnit] = findValues
  case object seconds extends TSUnit("sec")
  case object millis  extends TSUnit("ms")
  case object nanos   extends TSUnit("ns")
  case object micros  extends TSUnit("micros")
}
