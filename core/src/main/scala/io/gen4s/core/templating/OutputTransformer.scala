package io.gen4s.core.templating

import enumeratum._

sealed abstract class OutputTransformer(override val entryName: String) extends EnumEntry {
  def transform(template: RenderedTemplate): RenderedTemplate
}

object OutputTransformer extends Enum[OutputTransformer] with CirceEnum[OutputTransformer] {

  val values: IndexedSeq[OutputTransformer] = findValues

  case object JsonMinify extends OutputTransformer("json-minify") {

    override def transform(template: RenderedTemplate): RenderedTemplate =
      template.asJson match {
        case Left(_)      => template
        case Right(value) => RenderedTemplate(value.noSpaces)
      }
  }

  case object JsonPrettify extends OutputTransformer("json-prettify") {

    override def transform(template: RenderedTemplate): RenderedTemplate =
      template.asJson match {
        case Left(_)      => template
        case Right(value) => RenderedTemplate(value.spaces2)
      }
  }
}
