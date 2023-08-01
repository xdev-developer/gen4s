package io.gen4s.core.outputs

import io.gen4s.core.Domain.*

import enumeratum.EnumEntry
import eu.timepit.refined.types.numeric.PosInt
import eu.timepit.refined.types.string.NonEmptyString

sealed trait Output

case class StdOutput() extends Output

case class KafkaOutput(
  topic: Topic,
  bootstrapServers: BootstrapServers,
  headers: Map[String, String] = Map.empty,
  batchSize: PosInt = PosInt.unsafeFrom(1000))
    extends Output

sealed abstract class HttpMethods(override val entryName: String) extends EnumEntry

object HttpMethods extends enumeratum.Enum[HttpMethods] {
  val values: IndexedSeq[HttpMethods] = findValues

  case object Post extends HttpMethods("POST")
  case object Put  extends HttpMethods("PUT")
}

sealed abstract class HttpContentTypes(override val entryName: String) extends EnumEntry

object HttpContentTypes extends enumeratum.Enum[HttpContentTypes] {
  val values: IndexedSeq[HttpContentTypes] = findValues

  case object ApplicationJson extends HttpContentTypes("application/json")
  case object TextPlain       extends HttpContentTypes("text/plain")
  case object TextCsv         extends HttpContentTypes("text/csv")
  case object TextXml         extends HttpContentTypes("text/xml")
}

case class HttpOutput(
  url: String,
  method: HttpMethods,
  parallelism: PosInt = PosInt.unsafeFrom(1),
  headers: Map[String, String] = Map.empty,
  contentType: HttpContentTypes = HttpContentTypes.TextPlain,
  stopOnError: Boolean = true
) extends Output

case class FsOutput(dir: NonEmptyString, filenamePattern: NonEmptyString) extends Output
