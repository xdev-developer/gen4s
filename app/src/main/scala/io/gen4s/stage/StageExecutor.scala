package io.gen4s.stage

import org.typelevel.log4cats.slf4j.Slf4jLogger

import cats.effect.kernel.Async
import cats.implicits.*

trait StageExecutor[F[_]] {
  def exec(): F[Unit]
  def preview(): F[Unit]
}

object StageExecutor {

  def make[F[_]: Async](): F[StageExecutor[F]] = Async[F].delay {
    new StageExecutor[F] {
      override def exec(): F[Unit] = Async[F].unit
      override def preview(): F[Unit] = {
        for {
          logger <- Slf4jLogger.create[F]
          _      <- logger.info("Running preview")
        } yield Async[F].unit
      }

      def main() = ???
    }
  }

}
