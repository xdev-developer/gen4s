package io.gen4s.outputs.avro

opaque type AvroDynamicKey   = Array[Byte]
opaque type AvroDynamicValue = Array[Byte]

object AvroDynamicKey {
  def apply(bytes: Array[Byte]): AvroDynamicKey        = bytes
  extension (p: AvroDynamicKey) def bytes: Array[Byte] = p
}

object AvroDynamicValue {
  def apply(bytes: Array[Byte]): AvroDynamicValue        = bytes
  extension (p: AvroDynamicValue) def bytes: Array[Byte] = p
}
