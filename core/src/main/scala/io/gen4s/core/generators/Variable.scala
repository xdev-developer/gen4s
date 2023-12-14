package io.gen4s.core.generators

import cats.implicits.*
import io.circe.Decoder
import io.circe.Encoder

object Variable {
  def apply(name: String): Variable        = name
  extension (v: Variable) def name: String = v
}

/**
 * Represents template variable reference - generator for what variable in template
 *
 * @param name
 */
opaque type Variable = String

given Encoder[Variable] = Encoder.encodeString.contramap[Variable](_.self)
given Decoder[Variable] = Decoder.decodeString.map(v => Variable(v))
