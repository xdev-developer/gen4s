package io.gen4s.core.templating

import org.apache.commons.text.StringSubstitutor

import scala.jdk.CollectionConverters.*

import io.gen4s.core.generators.GeneratedValue
import io.gen4s.core.generators.Variable
import io.gen4s.core.templating.TextTemplate.stripQuotes

object TextTemplate {
  private val Quote          = "\""
  private val VariablePrefix = "{{"
  private val VariableSuffix = "}}"

  def stripQuotes(in: String): String = {
    in.stripPrefix(Quote)
      .stripSuffix(Quote)
  }
}

/**
 * Text template
 *
 * @param source source raw template
 * @param context template context
 */
case class TextTemplate(source: SourceTemplate, context: TemplateContext, transformers: Set[OutputTransformer])
    extends Template {

  override def render(): RenderedTemplate = {
    val localValues: Map[Variable, GeneratedValue] =
      context.generators
        .map(g => g.variable -> g.gen())
        .toMap

    val values = (context.globalValues ++ localValues).map { case (v, c) =>
      v.name -> stripQuotes(c.v.noSpaces)
    }

    RenderedTemplate(
      StringSubstitutor
        .replace(source.content, values.asJava, TextTemplate.VariablePrefix, TextTemplate.VariableSuffix)
    ).transform(transformers)
  }

}
