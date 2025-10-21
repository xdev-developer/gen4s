package io.gen4s.outputs.processors

import cats.Applicative
import cats.effect.kernel.Async
import io.gen4s.core.Domain
import io.gen4s.core.templating.Template
import io.gen4s.outputs.{HttpMethods, HttpOutput}

import sttp.client3.asynchttpclient.cats.AsyncHttpClientCatsBackend

/**
 * Sends generated content to HTTP endpoint
 * @tparam F
 */
class HttpOutputProcessor[F[_]: Async] extends OutputProcessor[F, HttpOutput] {

  override def process(
    n: Domain.NumberOfSamplesToGenerate,
    flow: fs2.Stream[F, Template],
    output: HttpOutput): F[Unit] = {
    import sttp.client3._
    import cats.implicits._

    val contentType: String = output.contentType.entryName
    val contentTypeHeader   = sttp.model.Header("Content-Type", contentType)

    val headers = output.headers.map { case (k, v) =>
      sttp.model.Header(k, v)
    }.toSeq :+ contentTypeHeader

    val uri = uri"${output.url}"

    AsyncHttpClientCatsBackend
      .resource[F]()
      .use { backend =>
        flow
          .mapAsync(output.parallelism.value) { template =>
            val req = output.method match {
              case HttpMethods.Post => basicRequest.post(uri)
              case HttpMethods.Put  => basicRequest.put(uri)
            }
            req
              .body(template.render().asByteArray)
              .headers(headers*)
              .send(backend)
              .flatMap[Boolean] { response =>
                if (response.code.isSuccess) {
                  Applicative[F].pure(true)
                } else {
                  val errorMsg = s"${output.method} ${output.url} completed with failure, status-code ${response.code}"
                  if (output.stopOnError) {
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
