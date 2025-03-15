package io.gen4s.outputs.avro

import org.apache.avro.Schema

import scala.annotation.nowarn

import cats.implicits.*

import tech.allegro.schema.json2avro.converter.JsonAvroConverter
import vulcan.{Avro, AvroError, Codec}
import vulcan.Codec.Aux

object AvroCodec {

  @nowarn
  def keyCodec(recordSchema: Schema): Aux[Avro.Record, AvroDynamicKey] = {
    val converter = new JsonAvroConverter()

    def encoder(key: AvroDynamicKey): Either[AvroError, Avro.Record] = {
      Either
        .catchNonFatal(converter.convertToGenericDataRecord(key.bytes, recordSchema))
        .leftMap(ex => AvroError.apply(s"Avro key encoder error: ${ex.getMessage}"))
    }

    def decoder(rec: Any, schema: Schema): Either[AvroError, AvroDynamicKey] =
      AvroDynamicKey(Array.emptyByteArray).asRight[AvroError]

    Codec.instance[Avro.Record, AvroDynamicKey](
      schema = recordSchema.asRight[AvroError],
      encode = encoder,
      decode = decoder
    )
  }

  @nowarn
  def valueCodec(recordSchema: Schema): Aux[Avro.Record, AvroDynamicValue] = {
    val converter = new JsonAvroConverter()

    def encoder(v: AvroDynamicValue): Either[AvroError, Avro.Record] = {
      Either
        .catchNonFatal(converter.convertToGenericDataRecord(v.bytes, recordSchema))
        .leftMap(ex => AvroError.apply(s"Avro value encoder error: ${ex.getMessage}"))
    }

    def decoder(rec: Any, schema: Schema): Either[AvroError, AvroDynamicValue] =
      AvroDynamicValue(Array.emptyByteArray).asRight[AvroError]

    Codec.instance[Avro.Record, AvroDynamicValue](
      schema = recordSchema.asRight[AvroError],
      encode = encoder,
      decode = decoder
    )
  }

}
