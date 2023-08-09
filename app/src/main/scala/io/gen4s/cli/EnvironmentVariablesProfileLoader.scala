package io.gen4s.cli

import java.io.File

import com.typesafe.config.*

import cats.syntax.all.*
import cats.MonadThrow
import io.gen4s.conf.*

import pureconfig.ConfigSource

trait EnvironmentVariablesProfileLoader[F[_]] {
  def fromFile(file: File): F[EnvVarsProfile]
  def applyProfile(p: EnvVarsProfile): F[Unit]
}

final case class EnvVarsProfile(source: EnvProfileConfig, vars: Map[String, String])

object EnvironmentVariablesProfileLoader {

  def make[F[_]: MonadThrow](): EnvironmentVariablesProfileLoader[F] = new EnvironmentVariablesProfileLoader[F] {

    override def fromFile(file: File): F[EnvVarsProfile] =
      (for {
        c <- Either.catchNonFatal(
               ConfigFactory.parseFile(file, ConfigParseOptions.defaults().setSyntax(ConfigSyntax.PROPERTIES))
             )
        map <- Either.catchNonFatal(ConfigSource.fromConfig(c).loadOrThrow[Map[String, String]])
      } yield EnvVarsProfile(EnvProfileConfig(c), map)).liftTo[F]

    override def applyProfile(p: EnvVarsProfile): F[Unit] =
      p.vars
        .foreach { case (k, v) => System.setProperty(k, v) }
        .pure[F]
  }
}
