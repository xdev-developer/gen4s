package io.gen4s.core.generators

import io.circe.*
import io.circe.syntax.*
import io.gen4s.core.generators.impl.*
import io.gen4s.core.generators.Generators.given

object Generator {

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

trait Generator {
  val variable: Variable
  def gen(): GeneratedValue
}
