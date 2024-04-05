package io.gen4s.test.outputs

import java.net.URI

import org.scalatest.funspec.AsyncFunSpec
import org.scalatest.matchers.should.Matchers
import org.scalatest.OptionValues
import org.testcontainers.containers.localstack.LocalStackContainer as JavaLocalStackContainer
import org.typelevel.log4cats.slf4j.Slf4jLogger
import org.typelevel.log4cats.Logger

import com.dimafeng.testcontainers.scalatest.TestContainersForAll
import com.dimafeng.testcontainers.LocalStackV2Container

import cats.data.NonEmptyList
import cats.effect.{IO, Sync}
import cats.effect.testing.scalatest.AsyncIOSpec
import io.gen4s.core.generators.Variable
import io.gen4s.core.streams.GeneratorStream
import io.gen4s.core.templating.{SourceTemplate, TemplateBuilder}
import io.gen4s.core.Domain.NumberOfSamplesToGenerate
import io.gen4s.generators.impl.TimestampGenerator
import io.gen4s.outputs.{OutputStreamExecutor, S3Output}
import io.laserdisc.pure.s3.tagless.{Interpreter as S3Interpreter, S3AsyncClientOp}

import eu.timepit.refined.types.string.NonEmptyString
import software.amazon.awssdk.endpoints.Endpoint
import software.amazon.awssdk.services.s3.model.{CreateBucketRequest, ListObjectsRequest}
import software.amazon.awssdk.services.s3.S3AsyncClient

class AWSOutputStreamTest
    extends AsyncFunSpec
    with AsyncIOSpec
    with TestContainersForAll
    with Matchers
    with OptionValues {

  private val template = SourceTemplate("{ timestamp: {{ts}} }")

  override type Containers = LocalStackV2Container

  override def startContainers(): Containers = {
    LocalStackV2Container
      .Def(
        services = List(JavaLocalStackContainer.Service.S3)
      )
      .start()
  }

  implicit def logger[F[_]: Sync]: Logger[F] = Slf4jLogger.getLogger[F]

  describe("AWS output stream") {

    it("Send records to AWS S3 bucket") {
      withContainers { localStack =>

        val credentials = localStack.staticCredentialsProvider.resolveCredentials()

        System.setProperty("aws.accessKeyId", credentials.accessKeyId())
        System.setProperty("aws.secretAccessKey", credentials.secretAccessKey())

        val executor = OutputStreamExecutor.make[IO]()

        val builder = TemplateBuilder.make(
          NonEmptyList.one(template),
          List(TimestampGenerator(Variable("ts")))
        )

        val output = S3Output(
          bucket = NonEmptyString.unsafeFrom("test-bucket"),
          key = NonEmptyString.unsafeFrom("test-key-%s.json"),
          region = localStack.region,
          endpoint = Some(
            Endpoint
              .builder()
              .url(localStack.endpointOverride(JavaLocalStackContainer.Service.S3))
              .build()
          )
        )

        val n = NumberOfSamplesToGenerate(5)

        s3Resource(output, localStack.endpointOverride(JavaLocalStackContainer.Service.S3)).use { s3 =>
          for {
            _       <- createBucket(s3, output.bucket)
            _       <- executor.write(n, GeneratorStream.stream[IO](n, builder), output)
            objects <- listObjects(s3, output.bucket)
          } yield objects.contents().size() shouldBe n.value
        }
      }
    }
  }

  private def s3Resource(output: S3Output, endpoint: URI) = S3Interpreter[IO]
    .S3AsyncClientOpResource(
      S3AsyncClient
        .builder()
        .endpointOverride(endpoint)
        .region(output.region)
    )

  private def createBucket(s3: S3AsyncClientOp[IO], bucket: NonEmptyString) =
    s3.createBucket(CreateBucketRequest.builder().bucket(bucket.value).build())

  private def listObjects(s3: S3AsyncClientOp[IO], bucket: NonEmptyString) =
    s3.listObjects(ListObjectsRequest.builder().bucket(bucket.value).build())

}
