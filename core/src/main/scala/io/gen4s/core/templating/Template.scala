package io.gen4s.core.templating

trait Renderable {
  def render(): RenderedTemplate
}

/**
 * Raw / initial template
 *
 * @param content
 */
case class SourceTemplate(content: String) extends AnyVal

/**
 * Produces json object
 */
case class JsonObjectTemplate()

case class TextTemplate()

/**
 * Final template - after all variables resolvings and transformations
 *
 * @param content
 */
case class RenderedTemplate(content: String)
