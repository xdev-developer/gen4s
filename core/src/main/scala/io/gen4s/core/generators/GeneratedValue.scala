package io.gen4s.core.generators

import io.circe.Decoder
import io.circe.Decoder.Result
import io.circe.Json

/**
 * Represents generated value
 *
 * @param v
 */
opaque type GeneratedValue = Json

object GeneratedValue {
  def apply(json: Json): GeneratedValue          = json
  def fromLong(v: Long): GeneratedValue          = GeneratedValue(Json.fromLong(v))
  def fromString(v: String): GeneratedValue      = GeneratedValue(Json.fromString(v))
  def fromBoolean(v: Boolean): GeneratedValue    = GeneratedValue(Json.fromBoolean(v))
  def fromInt(v: Int): GeneratedValue            = GeneratedValue(Json.fromInt(v))
  def fromDecimal(v: BigDecimal): GeneratedValue = GeneratedValue(Json.fromBigDecimal(v))
  def fromSeq(v: Seq[Json]): GeneratedValue      = GeneratedValue(Json.arr(v: _*))

  extension (gv: GeneratedValue) {
    def as[T](using d: Decoder[T]): Result[T] = v.as[T]
    def v: Json                               = gv
  }
}
