package io.gen4s

import cats.effect.{ExitCode, IO, IOApp}

object App extends IOApp {

  override def run(args: List[String]): IO[ExitCode] =
    IO.delay(ExitCode.Success)
}
