package io.gen4s.outputs.avro

import org.apache.avro.Schema

import scala.annotation.nowarn

import cats.implicits.*

import tech.allegro.schema.json2avro.converter.JsonAvroConverter
import vulcan.{Avro, AvroError, Codec}

object AvroCodec {

  private val empty = Array.emptyByteArray.asRight[AvroError]

  @nowarn
  def codec(recordSchema: Schema) = {
    val converter = new JsonAvroConverter()

    def encoder(p: Array[Byte]): Either[AvroError, Avro.Record] = {
      Either
        .catchNonFatal {
          converter.convertToGenericDataRecord(p, recordSchema)
        }
        .leftMap(ex => AvroError.apply(s"Avro encoder error: ${ex.getMessage}"))
    }

    def decoder(p: Any, schema: Schema): Either[AvroError, Array[Byte]] = empty

    Codec.instance[Avro.Record, Array[Byte]](
      schema = recordSchema.asRight[AvroError],
      encode = encoder,
      decode = decoder
    )
  }

}
