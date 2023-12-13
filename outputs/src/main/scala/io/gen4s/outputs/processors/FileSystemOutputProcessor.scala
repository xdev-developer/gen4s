package io.gen4s.outputs.processors

import cats.effect.kernel.Sync
import io.gen4s.core.templating.Template
import io.gen4s.core.Domain
import io.gen4s.outputs.FsOutput

import fs2.io.file.{Files, Path}
import fs2.text

/**
 * Writes generated content into files
 * @tparam F
 */
class FileSystemOutputProcessor[F[_]: Sync: Files] extends OutputProcessor[F, FsOutput] {

  override def process(
    n: Domain.NumberOfSamplesToGenerate,
    flow: fs2.Stream[F, Template],
    output: FsOutput): F[Unit] = {
    flow
      .map(_.render().asString)
      .intersperse("\n")
      .through(text.utf8.encode)
      .through(Files[F].writeAll(Path.fromNioPath(output.path())))
      .compile
      .drain
  }
}
