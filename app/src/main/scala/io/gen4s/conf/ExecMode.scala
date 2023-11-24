package io.gen4s.conf

import enumeratum.*

sealed abstract class ExecMode extends EnumEntry

object ExecMode extends Enum[ExecMode] {

  val values: IndexedSeq[ExecMode] = findValues

  case object Run     extends ExecMode
  case object Preview extends ExecMode
}
