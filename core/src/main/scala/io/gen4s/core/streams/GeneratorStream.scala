package io.gen4s.core.streams

import scala.util.Random

import cats.Applicative
import io.gen4s.core.templating.Template
import io.gen4s.core.templating.TemplateBuilder
import io.gen4s.core.Domain.NumberOfSamplesToGenerate

object GeneratorStream {

  def stream[F[_]: Applicative](
    n: NumberOfSamplesToGenerate,
    templateBuilder: TemplateBuilder,
    pickRandomTemplateFromList: Boolean = false): fs2.Stream[F, Template] = {
    val templates     = templateBuilder.build()
    val templatesSize = templates.size

    def pickTemplate(index: Int): Option[Template] = {
      if (pickRandomTemplateFromList) {
        templates.lift(Random.nextInt(templatesSize))
      } else {
        templates.lift(index % templatesSize)
      }
    }

    fs2.Stream
      .range(0, n.value)
      .flatMap { idx =>
        fs2.Stream
          .emit[F, Option[Template]](pickTemplate(idx))
          .unNone
      }
  }
}
