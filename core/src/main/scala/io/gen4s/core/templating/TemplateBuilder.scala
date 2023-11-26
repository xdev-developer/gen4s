package io.gen4s.core.templating

import io.gen4s.core.generators.GeneratedValue
import io.gen4s.core.generators.Generator
import io.gen4s.core.generators.Variable

/**
 * //FIXME: Better name?
 */
trait TemplateBuilder {
  def build(): List[Template]
}

object TemplateBuilder {

  def make(
    sourceTemplates: List[SourceTemplate],
    generators: List[Generator],
    globalVariables: List[Variable],
    transformers: Set[OutputTransformer]): TemplateBuilder =
    new TemplateBuilder {

      override def build(): List[Template] = {
        val (global, local) = generators.partition(g => globalVariables.contains(g.variable))

        val globalValues: Map[Variable, GeneratedValue] =
          generators
            .filter(s => globalVariables.contains(s.variable))
            .map(g => g.variable -> g.gen())
            .toMap

        sourceTemplates.map(source => TextTemplate(source, globalValues, local, transformers))
      }

    }
}
