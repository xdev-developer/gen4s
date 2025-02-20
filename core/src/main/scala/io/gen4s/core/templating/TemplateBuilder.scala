package io.gen4s.core.templating

import cats.data.NonEmptyList
import io.gen4s.core.generators.GeneratedValue
import io.gen4s.core.generators.Generator
import io.gen4s.core.generators.Variable
import io.gen4s.core.InputRecord

/**
 * FIXME: Better name?
 */
trait TemplateBuilder {
  def build(): List[Template]
}

object TemplateBuilder {

  def make(
    sourceTemplates: NonEmptyList[SourceTemplate],
    generators: List[Generator] = List.empty[Generator],
    globalVariables: Set[Variable] = Set.empty[Variable],
    userInput: Map[Variable, GeneratedValue] = Map.empty[Variable, GeneratedValue],
    transformers: Set[OutputTransformer] = Set.empty[OutputTransformer]): TemplateBuilder = {
    new TemplateBuilder() {

      override def build(): List[Template] = {
        // Split global vars (generated once per run) vs local vars (generated for each sample)
        val (global, local) = generators.partition(g => globalVariables.contains(g.variable))

        // Generate global vars once
        val globalValues: Map[Variable, GeneratedValue] = global.map(g => g.variable -> g.gen()).toMap

        // Filter out/replace locals with user input records, so input records has higher priority
        val userInputVars = userInput.keys.toList
        val toGenerate    = local.filterNot(g => userInputVars.contains(g.variable))

        sourceTemplates.toList
          .map(source => TextTemplate(source, TemplateContext(globalValues ++ userInput, toGenerate), transformers))
      }
    }
  }

  def ofRecordsStream(
    sourceTemplates: NonEmptyList[SourceTemplate],
    recordsStream: NonEmptyList[InputRecord],
    generators: List[Generator] = List.empty[Generator],
    globalVariables: Set[Variable] = Set.empty[Variable],
    userInput: Map[Variable, GeneratedValue] = Map.empty[Variable, GeneratedValue],
    transformers: Set[OutputTransformer] = Set.empty[OutputTransformer]
  ): TemplateBuilder = {
    new TemplateBuilder() {

      override def build(): List[Template] = {
        // Split global vars (generated once per run) vs local vars (generated for each sample)
        val (global, local) = generators.partition(g => globalVariables.contains(g.variable))

        // Filter out/replace locals with records in records stream (csv), so input records has higher priority
        val inputRecordsVars = recordsStream.toList.flatMap(_.fields.keys)
        val userInputVars    = userInput.keys.toList

        val toGenerate = local
          .filterNot(g => inputRecordsVars.contains(g.variable))
          .filterNot(g => userInputVars.contains(g.variable))

        // Generate global vars
        val globalValues: Map[Variable, GeneratedValue] = global.map(g => g.variable -> g.gen()).toMap

        (for {
          r      <- recordsStream
          source <- sourceTemplates
        } yield TextTemplate(
          source,
          TemplateContext(globalValues ++ r.fields ++ userInput, toGenerate),
          transformers
        )).toList

      }
    }
  }

}
