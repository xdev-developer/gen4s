package io.gen4s.core.templating

import org.apache.commons.text.StringSubstitutor

import scala.jdk.CollectionConverters.*

import io.gen4s.core.generators.GeneratedValue
import io.gen4s.core.generators.Generator
import io.gen4s.core.generators.Variable

/**
 * Text template
 *
 * @param source source raw template
 * @param globalValues list of global values
 * @param generators list of generators
 */
case class TextTemplate(
  source: SourceTemplate,
  globalValues: Map[Variable, GeneratedValue],
  generators: List[Generator],
  transformers: Set[OutputTransformer])
    extends Template {

  private val Quote          = "\""
  private val VariablePrefix = "{{"
  private val VariableSuffix = "}}"

  override def render(): RenderedTemplate = {
    val localValues: Map[Variable, GeneratedValue] =
      generators
        .map(g => g.variable -> g.gen())
        .toMap

    val values = (globalValues ++ localValues).map { case (v, c) =>
      v.name -> c.v.noSpaces
        .stripPrefix(Quote)
        .stripSuffix(Quote)
    }

    RenderedTemplate(
      StringSubstitutor
        .replace(source.content, values.asJava, VariablePrefix, VariableSuffix)
    ).transform(transformers)
  }

}
