package io.gen4s.conf

import cats.Eq

import enumeratum.*

sealed abstract class ExecMode(override val entryName: String) extends EnumEntry

object ExecMode extends Enum[ExecMode] {

  given Eq[ExecMode] = Eq.fromUniversalEquals

  val values: IndexedSeq[ExecMode] = findValues

  case object Run         extends ExecMode("Run")
  case object Preview     extends ExecMode("Preview")
  case object RunScenario extends ExecMode("Run Scenario")
}
