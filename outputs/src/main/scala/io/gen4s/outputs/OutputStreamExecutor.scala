package io.gen4s.outputs

import org.typelevel.log4cats.Logger

import cats.effect.kernel.Async
import cats.effect.std.Console as EffConsole
import cats.implicits.*
import io.gen4s.core.Domain.NumberOfSamplesToGenerate
import io.gen4s.core.templating.Template
import io.gen4s.outputs.processors.*
import io.gen4s.outputs.processors.aws.S3OutputProcessor
import io.gen4s.outputs.processors.kafka.*

import fs2.io.file.Files

trait OutputStreamExecutor[F[_]] {

  /**
   * Writes generated data to output
   *
   * @param n number of samples to generate
   * @param flow flow
   * @param output output
   * @return unit
   */
  def write(n: NumberOfSamplesToGenerate, flow: fs2.Stream[F, Template], output: Output): F[Unit]

}

object OutputStreamExecutor {

  /**
   * Factory method for creating an instance of OutputStreamExecutor.
   *
   * This method creates an instance of OutputStreamExecutor with a set of predefined processors for different output types.
   *
   * @return an instance of OutputStreamExecutor.
   */
  def make[F[_]: Async: EffConsole: Files: Logger](): OutputStreamExecutor[F] = new OutputStreamExecutor[F] {

    private val stdProcessor           = new StdOutputProcessor[F]()
    private val fsProcessor            = new FileSystemOutputProcessor[F]()
    private val kafkaProcessor         = new KafkaOutputProcessor[F]()
    private val kafkaAvroProcessor     = new KafkaAvroOutputProcessor[F]()
    private val kafkaProtobufProcessor = new KafkaProtobufOutputProcessor[F]()
    private val httpOutputProcessor    = new HttpOutputProcessor[F]()
    private val s3OutputProcessor      = new S3OutputProcessor[F]()

    override def write(n: NumberOfSamplesToGenerate, flow: fs2.Stream[F, Template], output: Output): F[Unit] = {
      val io = output match {
        case out: StdOutput           => stdProcessor.process(n, flow, out)
        case out: FsOutput            => fsProcessor.process(n, flow, out)
        case out: HttpOutput          => httpOutputProcessor.process(n, flow, out)
        case out: KafkaOutput         => kafkaProcessor.process(n, flow, out)
        case out: KafkaAvroOutput     => kafkaAvroProcessor.process(n, flow, out)
        case out: KafkaProtobufOutput => kafkaProtobufProcessor.process(n, flow, out)
        case out: S3Output            => s3OutputProcessor.process(n, flow, out)
      }
      Logger[F].info(s"Writing data to ${output.description()}") *> io
    }
  }
}
