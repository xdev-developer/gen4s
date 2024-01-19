package io.gen4s.outputs.processors

import cats.effect.kernel.Sync
import cats.effect.std.Console as EffConsole
import io.gen4s.core.templating.Template
import io.gen4s.core.Domain
import io.gen4s.outputs.StdOutput

class StdOutputProcessor[F[_]: Sync: EffConsole] extends OutputProcessor[F, StdOutput] {

  override def process(
    n: Domain.NumberOfSamplesToGenerate,
    flow: fs2.Stream[F, Template],
    output: StdOutput): F[Unit] = {
    flow
      .map(_.render())
      .printlns
      .compile
      .drain
  }
}
