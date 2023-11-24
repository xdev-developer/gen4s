package io.gen4s.test.outputs

import java.io.File

import org.scalatest.funspec.AsyncFunSpec
import org.scalatest.matchers.should.Matchers

import scala.io.Source

import cats.effect.testing.scalatest.AsyncIOSpec
import cats.effect.IO
import io.gen4s.core.generators.impl.TimestampGenerator
import io.gen4s.core.generators.Variable
import io.gen4s.core.streams.GeneratorStream
import io.gen4s.core.templating.{SourceTemplate, TemplateBuilder}
import io.gen4s.core.Domain.NumberOfSamplesToGenerate
import io.gen4s.outputs.{FsOutput, OutputStreamExecutor}

import eu.timepit.refined.types.string.NonEmptyString

class FileSystemOutputTest extends AsyncFunSpec with AsyncIOSpec with Matchers {

  private val template = SourceTemplate("{ timestamp: {{ts}} }")

  describe("FileSystem Output") {
    it("Write to file") {
      val streams = OutputStreamExecutor.make[IO]()
      val builder = TemplateBuilder.make(
        List(template),
        List(TimestampGenerator(Variable("ts"))),
        Nil
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
