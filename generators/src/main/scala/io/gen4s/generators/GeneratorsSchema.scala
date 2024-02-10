package io.gen4s.generators

import io.circe.derivation.ConfiguredCodec
import io.gen4s.core.generators.Generator
import io.gen4s.generators.codec.given

final case class GeneratorsSchema(generators: List[Generator]) derives ConfiguredCodec
