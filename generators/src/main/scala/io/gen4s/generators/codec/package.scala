package io.gen4s.generators

import io.circe.*
import io.circe.derivation.Configuration
import io.circe.syntax.*
import io.gen4s.core.generators.Generator
import io.gen4s.generators.impl.*

package object codec {

  given Configuration = Configuration.default

  export io.circe.derivation.ConfiguredCodec

  export Configuration.given

  private val Type = "type"

  given Encoder[Generator] = {
    case g: TimestampGenerator     => withType(g.asJson, Generators.TimeStamp)
    case g: DatetimeGenerator      => withType(g.asJson, Generators.Date)
    case g: BooleanGenerator       => withType(g.asJson, Generators.Boolean)
    case g: IntNumberGenerator     => withType(g.asJson, Generators.Integer)
    case g: DoubleNumberGenerator  => withType(g.asJson, Generators.Double)
    case g: StringGenerator        => withType(g.asJson, Generators.String)
    case g: UuidGenerator          => withType(g.asJson, Generators.UUID)
    case g: StaticValueGenerator   => withType(g.asJson, Generators.Static)
    case g: IpGenerator            => withType(g.asJson, Generators.Ip)
    case g: EnvVarGenerator        => withType(g.asJson, Generators.EnvVar)
    case g: StringPatternGenerator => withType(g.asJson, Generators.Pattern)
    case g: EnumGenerator          => withType(g.asJson, Generators.Enum)
    case g: ListGenerator          => withType(g.asJson, Generators.List)
  }

  given Decoder[Generator] = (cursor: HCursor) =>
    for {
      t <- cursor.get[Generators](Type)
      result <- t match {
                  case Generators.TimeStamp => cursor.as[TimestampGenerator]
                  case Generators.Date      => cursor.as[DatetimeGenerator]

                  case Generators.List => cursor.as[ListGenerator]
                  case Generators.Enum => cursor.as[EnumGenerator]

                  case Generators.Boolean => cursor.as[BooleanGenerator]
                  case Generators.Integer => cursor.as[IntNumberGenerator]
                  case Generators.Double  => cursor.as[DoubleNumberGenerator]

                  case Generators.UUID => cursor.as[UuidGenerator]

                  case Generators.String  => cursor.as[StringGenerator]
                  case Generators.Static  => cursor.as[StaticValueGenerator]
                  case Generators.Pattern => cursor.as[StringPatternGenerator]

                  case Generators.EnvVar => cursor.as[EnvVarGenerator]

                  case Generators.Ip  => cursor.as[IpGenerator]
                  case Generators.Mac => ???
                }
    } yield result

  private def withType(json: Json, t: Generators): Json = json.deepMerge(Json.obj(Type -> Json.fromString(t.entryName)))
}
