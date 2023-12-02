package io.gen4s.core

import io.gen4s.core.generators.{GeneratedValue, Variable}

object InputRecord {

  def of(variable: Variable, value: GeneratedValue): InputRecord = {
    InputRecord(Map(variable -> value))
  }
}

case class InputRecord(fields: Map[Variable, GeneratedValue])
