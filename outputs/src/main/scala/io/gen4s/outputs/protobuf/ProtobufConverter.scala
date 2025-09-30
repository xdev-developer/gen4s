package io.gen4s.outputs.protobuf

import java.io.FileInputStream

import com.google.protobuf.util.JsonFormat
import com.google.protobuf.Descriptors.Descriptor
import com.google.protobuf.DynamicMessage

import cats.effect.kernel.{Resource, Sync}
import io.confluent.kafka.schemaregistry.protobuf.dynamic.DynamicSchema
import io.gen4s.core.templating.RenderedTemplate
import io.gen4s.outputs.ProtobufDescriptorConfig

object ProtobufConverter {

  def loadDescriptor[F[_]: Sync](config: ProtobufDescriptorConfig): F[Descriptor] = {
    Resource
      .fromAutoCloseable(Sync[F].delay(new FileInputStream(config.file)))
      .use { descriptorStream =>
        Sync[F].catchNonFatal {
          DynamicSchema
            .parseFrom(descriptorStream)
            .getMessageDescriptor(config.messageType)
        }
      }
  }

  def toDynamicRecord(descriptor: Descriptor, template: RenderedTemplate): DynamicMessage = {
    val builder = com.google.protobuf.DynamicMessage.newBuilder(descriptor)
    JsonFormat
      .parser()
      .ignoringUnknownFields()
      .merge(template.value, builder)

    builder.build()
  }

}
