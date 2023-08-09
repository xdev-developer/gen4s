package io.gen4s.cli

import java.io.File

import io.gen4s.conf.ExecMode
import io.gen4s.core.Domain.NumberOfSamplesToGenerate

final case class Args(
  mode: ExecMode = ExecMode.Run,
  numberOfSamplesToGenerate: NumberOfSamplesToGenerate = NumberOfSamplesToGenerate(1),
  configFile: File = new File("."),
  profileFile: Option[File] = None
)
