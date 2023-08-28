package io.gen4s.outputs

object KafkaProducerConfig {
  sealed abstract class CompressionTypes(override val entryName: String) extends enumeratum.EnumEntry

  object CompressionTypes extends enumeratum.Enum[CompressionTypes] {
    val values: IndexedSeq[CompressionTypes] = findValues
    case object snappy extends CompressionTypes("snappy")
    case object none   extends CompressionTypes("none")
    case object gzip   extends CompressionTypes("gzip")
    case object lz4    extends CompressionTypes("lz4")
  }

  def default: KafkaProducerConfig =
    KafkaProducerConfig(
      compressionType = CompressionTypes.snappy,
      lingerMs = 100,
      maxBatchSizeBytes = 16384 * 4,
      maxRequestSizeBytes = 1048576L,
      inFlightRequests = 5
    )
}

case class KafkaProducerConfig(
  compressionType: KafkaProducerConfig.CompressionTypes,
  lingerMs: Long,
  maxBatchSizeBytes: Int,
  maxRequestSizeBytes: Long,
  inFlightRequests: Int)
