package io.gen4s.core.generators

import io.circe.Decoder
import io.circe.Decoder.Result
import io.circe.Json

opaque type GeneratedValue = Json

object GeneratedValue {
  def apply(json: Json): GeneratedValue          = json
  def fromLong(v: Long): GeneratedValue          = GeneratedValue(Json.fromLong(v))
  def fromString(v: String): GeneratedValue      = GeneratedValue(Json.fromString(v))
  def fromBoolean(v: Boolean): GeneratedValue    = GeneratedValue(Json.fromBoolean(v))
  def fromInt(v: Int): GeneratedValue            = GeneratedValue(Json.fromInt(v))
  def fromDecimal(v: BigDecimal): GeneratedValue = GeneratedValue(Json.fromBigDecimal(v))
  def fromSeq(v: Seq[Json]): GeneratedValue      = GeneratedValue(Json.arr(v*))
  def nullValue(): GeneratedValue                = GeneratedValue(Json.Null)

  extension (gv: GeneratedValue) {
    inline def as[T](using d: Decoder[T]): Result[T] = v.as[T]
    inline def v: Json                               = gv
  }
}
