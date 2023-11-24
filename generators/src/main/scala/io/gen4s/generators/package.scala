package io.gen4s

import io.circe.*
import io.circe.{Decoder, Encoder, HCursor, Json}
import io.circe.derivation.Configuration
import io.circe.syntax.*
import io.gen4s.core.generators.Generator
import io.gen4s.generators.impl.TimestampGenerator

package object generators {

  given Configuration = Configuration.default

  export io.circe.derivation.ConfiguredCodec

  export Configuration.given

  private val Type = "type"

  given Encoder[Generator] = { case g: TimestampGenerator =>
    withType(g.asJson, Generators.TimeStamp)
  }

  given Decoder[Generator] = (cursor: HCursor) =>
    for {
      t <- cursor.get[Generators](Type)
      result <- t match {
                  case Generators.TimeStamp => cursor.as[TimestampGenerator]
                  case Generators.Date      => ???

                  case Generators.Array => ???
                  case Generators.Enum  => ???

                  case Generators.Boolean => ???
                  case Generators.Integer => ???
                  case Generators.Double  => ???

                  case Generators.UUID => ???

                  case Generators.String  => ???
                  case Generators.Static  => ???
                  case Generators.Pattern => ???

                  case Generators.EnvVar => ???

                  case Generators.Ip  => ???
                  case Generators.Mac => ???
                }
    } yield result

  private def withType(json: Json, t: Generators): Json = json.deepMerge(Json.obj(Type -> Json.fromString(t.entryName)))
}
