package io.gen4s.conf

import pureconfig.*
import pureconfig.generic.derivation.default.*

case class StageConfig(input: InputConfig, output: OutputConfig) derives ConfigReader
