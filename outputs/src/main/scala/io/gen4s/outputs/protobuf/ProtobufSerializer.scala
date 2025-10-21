package io.gen4s.outputs.protobuf

import com.google.protobuf.Message

import cats.effect.Sync
import cats.effect.kernel.Resource

import fs2.kafka.{KeySerializer, Serializer, ValueSerializer}

final class ProtobufSerializer[T <: Message] private[protobuf] {

  def forKey[F[_]](settings: ProtobufSettings[F, T])(implicit F: Sync[F]): Resource[F, KeySerializer[F, T]] =
    Resource
      .fromAutoCloseable(settings.createProtobufSerializer(isKey = true))
      .map { serializer =>
        Serializer.instance[F, T] { (topic, _, a) =>
          F.pure(serializer.serialize(topic, a))
        }
      }

  def forValue[F[_]](settings: ProtobufSettings[F, T])(implicit F: Sync[F]): Resource[F, ValueSerializer[F, T]] =
    Resource
      .fromAutoCloseable(settings.createProtobufSerializer(isKey = false))
      .map { serializer =>
        Serializer.instance[F, T] { (topic, _, a) =>
          F.pure(serializer.serialize(topic, a))
        }
      }
}
