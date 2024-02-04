package io.gen4s.outputs

import org.typelevel.log4cats.Logger

import cats.effect.kernel.Async
import cats.effect.std.Console as EffConsole
import cats.implicits.*
import io.gen4s.core.templating.Template
import io.gen4s.core.Domain.NumberOfSamplesToGenerate
import io.gen4s.outputs.processors.*
import io.gen4s.outputs.processors.kafka.{KafkaAvroOutputProcessor, KafkaOutputProcessor, KafkaProtobufOutputProcessor}

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

  def make[F[_]: Async: EffConsole: Files: Logger](): OutputStreamExecutor[F] = new OutputStreamExecutor[F] {

    private val stdProcessor           = new StdOutputProcessor[F]()
    private val fsProcessor            = new FileSystemOutputProcessor[F]()
    private val kafkaProcessor         = new KafkaOutputProcessor[F]()
    private val kafkaAvroProcessor     = new KafkaAvroOutputProcessor[F]()
    private val kafkaProtobufProcessor = new KafkaProtobufOutputProcessor[F]()
    private val httpOutputProcessor    = new HttpOutputProcessor[F]()

    override def write(n: NumberOfSamplesToGenerate, flow: fs2.Stream[F, Template], output: Output): F[Unit] =
      output match {
        case out: StdOutput => Logger[F].info("Writing data to std-output") *> stdProcessor.process(n, flow, out)
        case out: FsOutput  => Logger[F].info(s"Writing data file ${out.path()}") *> fsProcessor.process(n, flow, out)
        case out: HttpOutput =>
          Logger[F].info(s"Writing data to HTTP endpoint ${out.url}") *>
            httpOutputProcessor.process(n, flow, out)

        case out: KafkaOutput =>
          Logger[F].info(s"Writing data to kafka brokers: ${out.bootstrapServers}, topic ${out.topic}") *>
            kafkaProcessor.process(n, flow, out)

        case out: KafkaAvroOutput =>
          Logger[F].info(
            s"Writing data to kafka brokers: ${out.bootstrapServers}, topic <${out.topic}>, registry: ${out.avroConfig.schemaRegistryUrl}"
          ) *>
            kafkaAvroProcessor.process(n, flow, out)

        case out: KafkaProtobufOutput => kafkaProtobufProcessor.process(n, flow, out)
      }
  }
}
