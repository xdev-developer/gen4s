package io.gen4s.core.templating

import io.gen4s.core.generators.GeneratedValue
import io.gen4s.core.generators.Generator
import io.gen4s.core.generators.Variable

trait TemplateGenerator {
  def generate(): List[Template]
}

object TemplateGenerator {

  def make(
    sourceTemplates: List[SourceTemplate],
    generators: List[Generator],
    globalVariables: List[Variable]): TemplateGenerator =
    new TemplateGenerator {

      override def generate(): List[Template] = {
        val (global, local) = generators.partition(g => globalVariables.contains(g.variable))

        val globalValues: Map[Variable, GeneratedValue] =
          generators
            .filter(s => globalVariables.contains(s.variable))
            .map(g => g.variable -> g.gen())
            .toMap

        sourceTemplates.map(source => TextTemplate(source, globalValues, local))
      }

    }
}
