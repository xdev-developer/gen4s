package io.gen4s.core.templating

import java.nio.charset.StandardCharsets

import cats.implicits.*
import io.circe.{Json, ParsingFailure}
import io.gen4s.core.Newtype

trait Template {
  def render(): RenderedTemplate
}

/**
 * Raw / initial template
 */
type SourceTemplate = SourceTemplate.Type
object SourceTemplate extends Newtype[String]

/**
 * Final template - after all variables resolvings and transformations
 */
type RenderedTemplate = RenderedTemplate.Type

object RenderedTemplate extends Newtype[String] {

  extension (rt: RenderedTemplate) {

    def asString: String = rt.value

    def asByteArray: Array[Byte] = {
      val bytes = rt.value.getBytes(StandardCharsets.UTF_8)
      if (bytes.isEmpty) " ".getBytes(StandardCharsets.UTF_8) else bytes
    }

    def asJson: Either[ParsingFailure, Json] = {
      import io.circe.parser.*
      parse(asString)
    }

    def asPrettyString: String = asJson.toOption.map(_.spaces2).getOrElse(asString)

    def asKeyValue: Either[ParsingFailure, (Array[Byte], RenderedTemplate)] = {

      def serializeKey(key: Json): Array[Byte] = {
        key.fold[Array[Byte]](
          jsonNull = Array.empty[Byte],
          jsonBoolean = b => Array((if (b) 1 else 0).toByte),
          jsonNumber = n =>
            n.toInt.match {
              case Some(data) =>
                Array(
                  (data >>> 24).byteValue,
                  (data >>> 16).byteValue,
                  (data >>> 8).byteValue,
                  data.byteValue
                )
              case None => Array.empty[Byte]
            },
          jsonString = s => s.getBytes(StandardCharsets.UTF_8),
          jsonArray = a => RenderedTemplate(TextTemplate.stripQuotes(key.noSpaces)).asByteArray,
          jsonObject = o => RenderedTemplate(TextTemplate.stripQuotes(key.noSpaces)).asByteArray
        )
      }

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
        serializeKey(key),
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
