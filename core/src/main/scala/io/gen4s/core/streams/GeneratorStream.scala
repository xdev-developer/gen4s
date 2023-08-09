package io.gen4s.core.streams

import io.gen4s.core.templating.RenderedTemplate
import io.gen4s.core.templating.TemplateGenerator
import io.gen4s.core.Domain.NumberOfSamplesToGenerate

object GeneratorStream {

  def stream[F[_]](
    n: NumberOfSamplesToGenerate,
    templateGenerator: TemplateGenerator): fs2.Stream[F, RenderedTemplate] = {
    val source = fs2.Stream
      .range(0, n.value)
      .flatMap(_ => fs2.Stream.emits(templateGenerator.generate()))

    source.map(_.render())
  }
}
