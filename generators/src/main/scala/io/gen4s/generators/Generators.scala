package io.gen4s.generators

import io.circe.{Decoder, Encoder}

import enumeratum.*

sealed abstract class Generators(override val entryName: String) extends EnumEntry

object Generators extends Enum[Generators] {

  given Encoder[Generators] = Circe.encoder(this)
  given Decoder[Generators] = Circe.decoder(this)

  val values: IndexedSeq[Generators] = findValues

  case object TimeStamp extends Generators("timestamp")
  case object Date      extends Generators("date")

  case object Array extends Generators("array")
  case object Enum  extends Generators("enum")

  case object Boolean extends Generators("boolean")
  case object Integer extends Generators("int")
  case object Double  extends Generators("double")

  case object UUID extends Generators("uuid")

  case object String  extends Generators("string")
  case object Static  extends Generators("static")
  case object Pattern extends Generators("pattern")

  case object EnvVar extends Generators("env-var")

  case object Ip  extends Generators("ip")
  case object Mac extends Generators("mac")

}
