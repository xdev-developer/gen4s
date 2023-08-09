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

/**
 * Final template - after all variables resolvings and transformations
 *
 * @param content
 */
case class RenderedTemplate(content: String)
