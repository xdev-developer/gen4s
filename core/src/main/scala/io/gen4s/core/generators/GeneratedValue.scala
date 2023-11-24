package io.gen4s.core.generators

import io.circe.Decoder
import io.circe.Json

object GeneratedValue {
  def fromLong(v: Long): GeneratedValue       = GeneratedValue(Json.fromLong(v))
  def fromString(v: String): GeneratedValue   = GeneratedValue(Json.fromString(v))
  def fromBoolean(v: Boolean): GeneratedValue = GeneratedValue(Json.fromBoolean(v))
  def fromInt(v: Int): GeneratedValue         = GeneratedValue(Json.fromInt(v))
}

/**
 * Represents generated value
 *
 * @param v
 */
final case class GeneratedValue(v: Json) extends AnyVal {
  def as[T](using d: Decoder[T]) = v.as[T]
}
