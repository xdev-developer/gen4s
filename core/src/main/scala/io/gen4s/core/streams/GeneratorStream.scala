package io.gen4s.core.streams

import scala.util.Random

import cats.Applicative
import io.gen4s.core.templating.Template
import io.gen4s.core.templating.TemplateBuilder
import io.gen4s.core.Domain.NumberOfSamplesToGenerate

object GeneratorStream {

  def stream[F[_]: Applicative](
    n: NumberOfSamplesToGenerate,
    templateBuilder: TemplateBuilder): fs2.Stream[F, Template] = {

    val templates = templateBuilder.build()
    fs2.Stream
      .range(0, n.value)
      .flatMap { _ =>
        fs2.Stream
          .emit[F, Option[Template]](templates.lift(Random.nextInt(templates.size)))
          .unNone
      }
  }
}
