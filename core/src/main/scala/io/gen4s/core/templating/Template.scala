package io.gen4s.core.templating

import cats.Show
import io.circe.{Json, ParsingFailure}

trait Template {
  def render(): RenderedTemplate
}

/**
 * Raw / initial template
 *
 * @param content
 */
case class SourceTemplate(content: String) extends AnyVal

object RenderedTemplate {
  given Show[RenderedTemplate] = Show.show[RenderedTemplate](_.asString)
}

/**
 * Final template - after all variables resolvings and transformations
 *
 * @param content
 */
case class RenderedTemplate(content: String) extends AnyVal {
  def asString: String = content

  def asByteArray: Array[Byte] = {
    val bytes = content.getBytes
    if (bytes.isEmpty) " ".getBytes() else bytes
  }

  def asJson: Either[ParsingFailure, Json] = {
    import io.circe.parser.*
    parse(asString)
  }

  def asPrettyString: String = asJson.toOption.map(_.spaces2).getOrElse(asString)

  /**
   * Apply template transformers
   *
   * @param transformers [[OutputTransformer]] set of transformers
   * @return transformed
   */
  def transform(transformers: Set[OutputTransformer]): RenderedTemplate =
    if (transformers.nonEmpty) {
      transformers.foldLeft(this) { case (template, transformer) =>
        transformer.transform(template)
      }
    } else this
}
