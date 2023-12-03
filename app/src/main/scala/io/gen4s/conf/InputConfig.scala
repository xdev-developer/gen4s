package io.gen4s.conf

import java.io.File

case class InputConfig(
  schema: File,
  template: File,
  csvRecords: Option[File] = None,
  decodeNewLineAsTemplate: Boolean = false
)
