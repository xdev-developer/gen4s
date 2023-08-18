package io.gen4s.core

import io.circe.derivation.ConfiguredCodec
import io.gen4s.core.generators.{*, given}

case class GeneratorsSchema(generators: List[Generator]) derives ConfiguredCodec
