package io.gen4s.outputs.processors.aws

import java.util.UUID

import org.typelevel.log4cats.Logger

import cats.effect.kernel.Async
import io.gen4s.core.templating.Template
import io.gen4s.core.Domain
import io.gen4s.outputs.processors.OutputProcessor
import io.gen4s.outputs.S3Output
import io.laserdisc.pure.s3.tagless.{Interpreter as S3Interpreter, S3AsyncClientOp}

import eu.timepit.refined.types.string.NonEmptyString
import fs2.aws.s3.models.Models.{BucketName, FileKey}
import fs2.aws.s3.S3
import software.amazon.awssdk.services.s3.S3AsyncClient

class S3OutputProcessor[F[_]: Async: Logger] extends OutputProcessor[F, S3Output] {

  override def process(
    n: Domain.NumberOfSamplesToGenerate,
    flow: fs2.Stream[F, Template],
    output: S3Output): F[Unit] = {

    s3Resource(output).map(S3.create[F]).use { s3 =>
      flow
        .flatMap { t =>
          fs2.Stream
            .emits(t.render().asByteArray)
            .through(
              s3.uploadFileMultipart(BucketName(output.bucket), formatKey(output.key), partSize = output.partSizeMb)
            )
            .evalMap(t => Logger[F].debug(s"eTag: $t"))
        }
        .compile
        .drain
    }
  }

  private def formatKey(key: NonEmptyString): FileKey = {
    FileKey(NonEmptyString.unsafeFrom(key.value.format(UUID.randomUUID())))
  }

  private def s3Resource(output: S3Output) = {
    S3Interpreter[F].S3AsyncClientOpResource {
      output.endpoint match {
        case Some(e) => S3AsyncClient.builder().endpointOverride(e.url()).region(output.region)
        case None    => S3AsyncClient.builder().region(output.region)
      }
    }
  }
}
