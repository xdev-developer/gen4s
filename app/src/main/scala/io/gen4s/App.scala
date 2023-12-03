package io.gen4s

import org.typelevel.log4cats.slf4j.Slf4jLogger

import cats.effect.*
import cats.effect.std.Console
import cats.implicits.*
import io.gen4s.cli.*
import io.gen4s.conf.*
import io.gen4s.stage.StageExecutor

import fs2.io.file.Files
import pureconfig.error.ConfigReaderException

object App extends IOApp {

  override def run(args: List[String]): IO[ExitCode] =
    new CliArgsParser().parse(args, Args()) match {
      case Some(a) =>
        program[IO](a)
          .handleErrorWith {
            case ConfigReaderException(errors) =>
              errors.toList
                .map(e => IO.println(e.origin) *> IO.println(redOut(e.description)))
                .sequence
                .as(ExitCode.Error)

            case TemplateValidationError(errors) => errors.map(e => IO.println(redOut(e))).sequence.as(ExitCode.Error)

            case ex =>
              val err = Option(ex.getMessage).getOrElse("unknown")
              IO.println(redOut(err)).as(ExitCode.Error)
          }
      case None => IO(ExitCode.Error)
    }

  private def program[F[_]: Async: Console: Files](args: Args) =
    for {
      logger         <- Slf4jLogger.create[F]
      _              <- logger.info("Running data generation stream")
      _              <- logger.info(s"Configuration file: ${args.configFile.getAbsolutePath}")
      _              <- logger.info(s"Execution mode: ${args.mode}")
      envVarsProfile <- loadEnvVarsProfile[F](args.profileFile)
      conf           <- StageConfigLoader.fromFile[F](args.configFile).withEnvProfile(envVarsProfile)
      executor       <- StageExecutor.make[F](args, conf)
      _              <- Async[F].whenA(args.mode == ExecMode.Run)(executor.exec())
      _              <- Async[F].whenA(args.mode == ExecMode.Preview)(executor.preview())

    } yield ExitCode.Success

  private def loadEnvVarsProfile[F[_]: Sync](in: Option[java.io.File]): F[EnvProfileConfig] = in match {
    case Some(f) =>
      val loader = EnvironmentVariablesProfileLoader.make[F]()
      for {
        p <- loader.fromFile(f)
        _ <- loader.applyProfile(p)
      } yield p.source

    case None => Sync[F].pure(EnvProfileConfig.empty)
  }

  private def redOut(msg: String): String = s"${scala.Console.RED}- $msg${scala.Console.RESET}"
}
