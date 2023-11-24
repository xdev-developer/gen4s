package io.gen4s.generators

import io.circe.derivation.ConfiguredCodec
import io.gen4s.core.generators.Generator

case class GeneratorsSchema(generators: List[Generator]) derives ConfiguredCodec
