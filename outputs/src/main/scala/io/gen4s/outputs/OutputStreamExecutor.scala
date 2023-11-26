package io.gen4s.outputs

import org.apache.kafka.clients.producer.ProducerConfig

import cats.effect.kernel.Async
import cats.effect.std.Console as EffConsole
import cats.Applicative
import io.gen4s.core.templating.RenderedTemplate
import io.gen4s.core.templating.Template
import io.gen4s.core.Domain.NumberOfSamplesToGenerate

import fs2.io.file.{Files, Path}
import fs2.kafka.Acks
import fs2.text
import sttp.client3.asynchttpclient.cats.AsyncHttpClientCatsBackend

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

  /**
   * Writes generated data into standard system output
   * @param flow main data generation flow
   * @return unit
   */
  def stdOutput(flow: fs2.Stream[F, Template]): F[Unit]

  /**
   * Writes generated data into file
   * @param flow   flow
   * @param output output config
   * @return unit
   */
  def fileSystemOutput(flow: fs2.Stream[F, Template], output: FsOutput): F[Unit]

  /**
   * Writes generated data into kafka topic
   *
   * @param n number of samples to generate
   * @param output output config
   * @param flow generator flow
   * @return unit
   */
  def kafkaOutput(n: NumberOfSamplesToGenerate, output: KafkaOutput, flow: fs2.Stream[F, Template]): F[Unit]

  def httpOutput(feed: fs2.Stream[F, Template], out: HttpOutput): F[Unit]
}

object OutputStreamExecutor {

  def make[F[_]: Async: EffConsole: Files](): OutputStreamExecutor[F] = new OutputStreamExecutor[F] {

    override def write(n: NumberOfSamplesToGenerate, flow: fs2.Stream[F, Template], output: Output): F[Unit] =
      output match {
        case _: StdOutput     => stdOutput(flow)
        case out: FsOutput    => fileSystemOutput(flow, out)
        case out: HttpOutput  => httpOutput(flow, out)
        case out: KafkaOutput => kafkaOutput(n, out, flow)
      }

    override def stdOutput(flow: fs2.Stream[F, Template]): F[Unit] =
      flow.map(_.render()).printlns.compile.drain

    override def fileSystemOutput(flow: fs2.Stream[F, Template], output: FsOutput): F[Unit] = {
      flow
        .map(_.render().asString)
        .intersperse("\n")
        .through(text.utf8.encode)
        .through(Files[F].writeAll(Path.fromNioPath(output.path())))
        .compile
        .drain
    }

    override def kafkaOutput(
      n: NumberOfSamplesToGenerate,
      output: KafkaOutput,
      flow: fs2.Stream[F, Template]): F[Unit] = {
      val producerSettings = fs2.kafka
        .ProducerSettings[F, Option[String], Array[Byte]]
        .withBootstrapServers(output.bootstrapServers.value)
        .withClientId("gen4s")
        .withAcks(Acks.All)
        .withBatchSize(output.kafkaProducerConfig.maxBatchSizeBytes)
        .withMaxInFlightRequestsPerConnection(output.kafkaProducerConfig.inFlightRequests)
        .withProperties(
          (ProducerConfig.COMPRESSION_TYPE_CONFIG, output.kafkaProducerConfig.compressionType.entryName),
          (ProducerConfig.LINGER_MS_CONFIG, output.kafkaProducerConfig.lingerMs.toString),
          (ProducerConfig.MAX_REQUEST_SIZE_CONFIG, output.kafkaProducerConfig.maxRequestSizeBytes.toString)
        )

      val groupSize = if (output.batchSize.value > n.value) output.batchSize.value else n.value

      val headers = {
        val list = output.headers.map { case (k, v) =>
          fs2.kafka.Header(k, v.getBytes)
        }.toList
        fs2.kafka.Headers(list: _*)
      }

      flow
        .chunkN(groupSize)
        .map { batch =>
          fs2.kafka.ProducerRecords.apply(batch.map { value =>
            fs2.kafka
              .ProducerRecord(output.topic.value, None, value.render().asByteArray)
              .withHeaders(headers)
          })
        }
        .through(fs2.kafka.KafkaProducer.pipe(producerSettings))
        .compile
        .drain
    }

    override def httpOutput(feed: fs2.Stream[F, Template], out: HttpOutput): F[Unit] = {
      import sttp.client3._
      import cats.implicits._

      val contentType: String = out.contentType.entryName
      val contentTypeHeader   = sttp.model.Header("Content-Type", contentType)

      val headers = out.headers.map { case (k, v) =>
        sttp.model.Header(k, v)
      }.toSeq :+ contentTypeHeader

      val uri = uri"${out.url}"

      AsyncHttpClientCatsBackend
        .resource[F]()
        .use { backend =>
          feed
            .mapAsync(out.parallelism.value) { template =>
              val req = out.method match {
                case HttpMethods.Post => basicRequest.post(uri)
                case HttpMethods.Put  => basicRequest.put(uri)
              }
              req
                .body(template.render().asByteArray)
                .headers(headers: _*)
                .send(backend)
                .flatMap[Boolean] { response =>
                  if (response.code.isSuccess) {
                    Applicative[F].pure(true)
                  } else {
                    val errorMsg = s"${out.method} ${out.url} completed with failure, status-code ${response.code}"
                    if (out.stopOnError) {
                      Async[F].raiseError(new Exception(errorMsg))
                    } else {
                      Applicative[F].pure(true)
                    }
                  }
                }
            }
            .compile
            .drain
        }
    }

  }
}