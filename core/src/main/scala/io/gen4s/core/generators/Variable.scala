package io.gen4s.core.generators

import io.circe.Decoder
import io.circe.Encoder

given Encoder[Variable] = Encoder.forProduct1("name")(o => o.name)
given Decoder[Variable] = Decoder.decodeString.map(v => Variable(v))

/**
 * Represents template variable reference - generator for what variable in template
 *
 * @param name
 */
final case class Variable(name: String) extends AnyVal
