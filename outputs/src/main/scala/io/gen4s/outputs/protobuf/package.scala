package io.gen4s.outputs

import com.google.protobuf.Message

package object protobuf {

  def protobufSerializer[T <: Message]: ProtobufSerializer[T] =
    new ProtobufSerializer[T]()

  def protobufDeserializer[T <: Message]: ProtobufDeserializer[T] =
    new ProtobufDeserializer[T]()
}
