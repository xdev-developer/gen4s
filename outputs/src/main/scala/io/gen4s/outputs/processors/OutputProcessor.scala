package io.gen4s.outputs.processors

import io.gen4s.core.Domain.NumberOfSamplesToGenerate
import io.gen4s.core.templating.Template

trait OutputProcessor[F[_], O] {

  /**
   * Writes generated content to output
   * @param n number of samples to generate
   * @param flow content stream
   * @param output output config
   * @return
   */
  def process(n: NumberOfSamplesToGenerate, flow: fs2.Stream[F, Template], output: O): F[Unit]
}
