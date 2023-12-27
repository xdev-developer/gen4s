package io.gen4s.core.templating

import cats.data.NonEmptyList
import io.gen4s.core.generators.GeneratedValue
import io.gen4s.core.generators.Generator
import io.gen4s.core.generators.Variable
import io.gen4s.core.InputRecord

/**
 * //FIXME: Better name?
 */
trait TemplateBuilder {
  def build(): List[Template]
}

object TemplateBuilder {

  def make(
    sourceTemplates: NonEmptyList[SourceTemplate],
    generators: List[Generator],
    globalVariables: Set[Variable],
    transformers: Set[OutputTransformer]): TemplateBuilder = {
    new TemplateBuilder() {

      override def build(): List[Template] = {
        val (global, local) = generators.partition(g => globalVariables.contains(g.variable))

        val globalValues: Map[Variable, GeneratedValue] =
          generators
            .filter(s => globalVariables.contains(s.variable))
            .map(g => g.variable -> g.gen())
            .toMap

        sourceTemplates.toList.map(source => TextTemplate(source, TemplateContext(globalValues, local), transformers))
      }
    }
  }

  def ofRecordsStream(
    sourceTemplates: NonEmptyList[SourceTemplate],
    generators: List[Generator],
    globalVariables: Set[Variable],
    recordsStream: NonEmptyList[InputRecord],
    transformers: Set[OutputTransformer]
  ): TemplateBuilder = {
    new TemplateBuilder() {

      override def build(): List[Template] = {
        val (global, local) = generators.partition(g => globalVariables.contains(g.variable))

        val globalValues: Map[Variable, GeneratedValue] =
          generators
            .filter(s => globalVariables.contains(s.variable))
            .map(g => g.variable -> g.gen())
            .toMap

        (for {
          r      <- recordsStream
          source <- sourceTemplates
        } yield TextTemplate(source, TemplateContext(globalValues ++ r.fields, local), transformers)).toList

      }
    }
  }

}
