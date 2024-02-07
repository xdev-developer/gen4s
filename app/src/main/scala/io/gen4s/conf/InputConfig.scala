package io.gen4s.conf

import java.io.File

import io.gen4s.core.generators.Variable

import pureconfig.*
import pureconfig.generic.derivation.default.*

final case class InputConfig(
  schema: File,
  template: File,
  csvRecords: Option[File] = None,
  globalVariables: Option[Set[Variable]] =
    None, // Wrap into option until pureconfig limitation of defaults will be fixed.
  decodeNewLineAsTemplate: Boolean = false
) derives ConfigReader {
  val globalVars: Set[Variable] = globalVariables.getOrElse(Set.empty[Variable])
}
