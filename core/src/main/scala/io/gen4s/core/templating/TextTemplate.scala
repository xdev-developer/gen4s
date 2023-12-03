package io.gen4s.core.templating

import org.apache.commons.text.StringSubstitutor

import scala.jdk.CollectionConverters.*

import io.gen4s.core.generators.GeneratedValue
import io.gen4s.core.generators.Variable

/**
 * Text template
 *
 * @param source source raw template
 * @param context template context
 */
case class TextTemplate(source: SourceTemplate, context: TemplateContext, transformers: Set[OutputTransformer])
    extends Template {

  private val Quote          = "\""
  private val VariablePrefix = "{{"
  private val VariableSuffix = "}}"

  override def render(): RenderedTemplate = {
    val localValues: Map[Variable, GeneratedValue] =
      context.generators
        .map(g => g.variable -> g.gen())
        .toMap

    val values = (context.globalValues ++ localValues).map { case (v, c) =>
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
