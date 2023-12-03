package io.gen4s.cli

import java.io.File

import org.apache.commons.io.FilenameUtils

import cats.effect.Sync
import cats.implicits.*
import io.gen4s.conf.*

trait StageConfigLoader[F[_]] {
  def withEnvProfile(p: EnvProfileConfig): F[StageConfig]
  def withEmptyEnvProfile(): F[StageConfig]
}

object StageConfigLoader {

  def fromFile[F[_]: Sync](file: File): StageConfigLoader[F] = new StageConfigLoader[F] {

    override def withEnvProfile(profileConf: EnvProfileConfig): F[StageConfig] = {
      import pureconfig.*
      import pureconfig.ConfigSource
      import pureconfig.module.catseffect.syntax.*

      ConfigSource
        .file(file)
        .withFallback(ConfigSource.fromConfig(profileConf.value))
        .loadF[F, StageConfig]()
        .map(resolveRootDirectory)
    }

    override def withEmptyEnvProfile(): F[StageConfig] = withEnvProfile(EnvProfileConfig.empty)

    private def resolveRootDirectory(conf: StageConfig): StageConfig = {

      def resolve(dir: String, in: File): File = {
        if (!in.exists()) {
          File(dir, FilenameUtils.getName(in.getAbsolutePath))
        } else in
      }

      val dir         = FilenameUtils.getFullPath(file.getAbsolutePath)
      val newSchema   = resolve(dir, conf.input.schema)
      val newTemplate = resolve(dir, conf.input.template)

      val newRecords = conf.input.csvRecords match {
        case Some(records) => resolve(dir, records).some
        case None          => None
      }

      conf.copy(input = conf.input.copy(schema = newSchema, template = newTemplate, csvRecords = newRecords))
    }
  }

}
