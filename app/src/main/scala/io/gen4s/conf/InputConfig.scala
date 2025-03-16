package io.gen4s.conf

import java.io.File

import io.gen4s.core.generators.Variable

import pureconfig.*
import pureconfig.generic.*
import pureconfig.generic.semiauto.deriveReader

object InputConfig {
  given ConfigReader[InputConfig] = deriveReader[InputConfig]
}

final case class InputConfig(
  schema: File,
  template: File,
  csvRecords: Option[File] = None,
  globalVariables: Set[Variable] = Set.empty[Variable],
  decodeNewLineAsTemplate: Boolean = false
)
