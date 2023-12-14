package io.gen4s.core.templating

import cats.implicits.*
import cats.Show
import io.circe.{Json, ParsingFailure}

trait Template {
  def render(): RenderedTemplate
}

object SourceTemplate {
  def apply(value: String): SourceTemplate          = value
  extension (v: SourceTemplate) def content: String = v
}

/**
 * Raw / initial template
 *
 * @param content source content
 */
opaque type SourceTemplate = String

/**
 * Final template - after all variables resolvings and transformations
 *
 * @param content rendered template content
 */
opaque type RenderedTemplate = String

object RenderedTemplate {

  given Show[RenderedTemplate] = Show.show[RenderedTemplate](_.asString)

  def apply(content: String): RenderedTemplate = content

  extension (rt: RenderedTemplate) {

    def asString: String = rt

    def asByteArray: Array[Byte] = {
      val bytes = rt.getBytes
      if (bytes.isEmpty) " ".getBytes() else bytes
    }

    def asJson: Either[ParsingFailure, Json] = {
      import io.circe.parser.*
      parse(asString)
    }

    def asPrettyString: String = asJson.toOption.map(_.spaces2).getOrElse(asString)

    def asKeyValue: Either[ParsingFailure, (RenderedTemplate, RenderedTemplate)] = {

      def extractField(key: String, obj: Json): Either[ParsingFailure, Json] = {
        obj.hcursor
          .downField(key)
          .as[Json]
          .leftMap(ex => ParsingFailure(s"Unable extract $key from $obj", ex))
      }

      for {
        json  <- asJson
        key   <- extractField("key", json)
        value <- extractField("value", json)
      } yield (
        RenderedTemplate(TextTemplate.stripQuotes(key.noSpaces)),
        RenderedTemplate(TextTemplate.stripQuotes(value.noSpaces))
      )
    }

    /**
     * Apply template transformers
     *
     * @param transformers [[OutputTransformer]] set of transformers
     * @return transformed
     */
    def transform(transformers: Set[OutputTransformer]): RenderedTemplate =
      if (transformers.nonEmpty) {
        transformers.foldLeft(rt) { case (template, transformer) =>
          transformer.transform(template)
        }
      } else rt
  }
}
