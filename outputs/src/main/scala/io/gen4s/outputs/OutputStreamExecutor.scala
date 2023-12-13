package io.gen4s.outputs

import cats.effect.kernel.Async
import cats.effect.std.Console as EffConsole
import io.gen4s.core.templating.Template
import io.gen4s.core.Domain.NumberOfSamplesToGenerate
import io.gen4s.outputs.processors.*
import io.gen4s.outputs.processors.kafka.{KafkaAvroOutputProcessor, KafkaOutputProcessor}

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

  def make[F[_]: Async: EffConsole: Files](): OutputStreamExecutor[F] = new OutputStreamExecutor[F] {

    private val stdProcessor        = new StdOutputProcessor[F]()
    private val fsProcessor         = new FileSystemOutputProcessor[F]()
    private val kafkaProcessor      = new KafkaOutputProcessor[F]()
    private val kafkaAvroProcessor  = new KafkaAvroOutputProcessor[F]()
    private val httpOutputProcessor = new HttpOutputProcessor[F]()

    override def write(n: NumberOfSamplesToGenerate, flow: fs2.Stream[F, Template], output: Output): F[Unit] =
      output match {
        case out: StdOutput       => stdProcessor.process(n, flow, out)
        case out: FsOutput        => fsProcessor.process(n, flow, out)
        case out: HttpOutput      => httpOutputProcessor.process(n, flow, out)
        case out: KafkaOutput     => kafkaProcessor.process(n, flow, out)
        case out: KafkaAvroOutput => kafkaAvroProcessor.process(n, flow, out)
      }
  }
}
