package io.gen4s.core.streams

import cats.effect.kernel.Async
import cats.effect.std.Console as EffConsole
import io.gen4s.core.outputs.FsOutput
import io.gen4s.core.outputs.HttpOutput
import io.gen4s.core.outputs.KafkaOutput
import io.gen4s.core.outputs.Output
import io.gen4s.core.outputs.StdOutput
import io.gen4s.core.templating.RenderedTemplate
import io.gen4s.core.templating.Template

trait OutputStreamExecutor[F[_]] {

  def write(flow: fs2.Stream[F, Template], output: Output): F[Unit]

  /**
   * Writes generated data into standard system output
   * @param flow main data generation flow
   * @return unit
   */
  def stdOutput(flow: fs2.Stream[F, Template]): F[Unit]
}

object OutputStreamExecutor {

  def make[F[_]: Async: EffConsole](): OutputStreamExecutor[F] = new OutputStreamExecutor[F] {

    override def write(flow: fs2.Stream[F, Template], output: Output): F[Unit] = output match {
      case o: StdOutput   => stdOutput(flow)
      case o: FsOutput    => stdOutput(flow) // FIXME: Implement
      case o: HttpOutput  => stdOutput(flow) // FIXME: Implement
      case o: KafkaOutput => stdOutput(flow) // FIXME: Implement
    }

    override def stdOutput(flow: fs2.Stream[F, Template]): F[Unit] =
      flow.map(_.render()).printlns.compile.drain

  }
}
