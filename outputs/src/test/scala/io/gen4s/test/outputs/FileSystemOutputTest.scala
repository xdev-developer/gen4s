package io.gen4s.test.outputs

import java.io.File

import org.scalatest.funspec.AsyncFunSpec
import org.scalatest.matchers.should.Matchers
import org.typelevel.log4cats.slf4j.Slf4jLogger
import org.typelevel.log4cats.Logger

import scala.io.Source

import cats.data.NonEmptyList
import cats.effect.{IO, Sync}
import cats.effect.testing.scalatest.AsyncIOSpec
import io.gen4s.core.generators.Variable
import io.gen4s.core.streams.GeneratorStream
import io.gen4s.core.templating.{OutputTransformer, SourceTemplate, TemplateBuilder}
import io.gen4s.core.Domain.NumberOfSamplesToGenerate
import io.gen4s.generators.impl.TimestampGenerator
import io.gen4s.outputs.{FsOutput, OutputStreamExecutor}

import eu.timepit.refined.types.string.NonEmptyString

class FileSystemOutputTest extends AsyncFunSpec with AsyncIOSpec with Matchers {

  private val template = SourceTemplate("{ timestamp: {{ts}} }")

  implicit def logger[F[_]: Sync]: Logger[F] = Slf4jLogger.getLogger[F]

  describe("FileSystem Output") {
    it("Write to file") {
      val streams = OutputStreamExecutor.make[IO]()
      val builder = TemplateBuilder.make(
        NonEmptyList.one(template),
        List(TimestampGenerator(Variable("ts"))),
        Set.empty[Variable],
        Set.empty[OutputTransformer]
      )

      val output = FsOutput(NonEmptyString.unsafeFrom("/tmp"), NonEmptyString.unsafeFrom("fs-out-fs2.json"))

      val n = NumberOfSamplesToGenerate(1)
      streams
        .write(n, GeneratorStream.stream[IO](n, builder), output)
        .asserting { _ =>
          readFile(output.path().toFile) should include("timestamp")
        }
    }
  }

  private def readFile(in: File): String = {
    val bufferedSource = Source.fromFile(in)
    try bufferedSource.mkString
    finally bufferedSource.close()
  }
}
