package io.gen4s.core.templating

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
}
