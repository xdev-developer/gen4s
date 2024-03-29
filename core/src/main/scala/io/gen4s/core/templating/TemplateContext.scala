package io.gen4s.core.templating

import io.gen4s.core.generators.{GeneratedValue, Generator, Variable}

final case class TemplateContext(globalValues: Map[Variable, GeneratedValue], generators: List[Generator])
