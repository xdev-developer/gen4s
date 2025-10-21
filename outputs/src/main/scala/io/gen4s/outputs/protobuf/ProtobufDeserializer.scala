package io.gen4s.outputs.protobuf

import com.google.protobuf.Message

import cats.effect.Sync
import cats.effect.kernel.Resource

import fs2.kafka.{Deserializer, KeyDeserializer, ValueDeserializer}

final class ProtobufDeserializer[T <: Message] private[protobuf] {

  def forKey[F[_]](settings: ProtobufSettings[F, T])(implicit F: Sync[F]): Resource[F, KeyDeserializer[F, T]] =
    Resource
      .fromAutoCloseable(settings.createProtobufDeserializer(isKey = true))
      .map { deserializer =>
        Deserializer.instance { (topic, _, bytes) =>
          F.pure(deserializer.deserialize(topic, bytes))
        }
      }

  def forValue[F[_]](settings: ProtobufSettings[F, T])(implicit F: Sync[F]): Resource[F, ValueDeserializer[F, T]] =
    Resource
      .fromAutoCloseable(settings.createProtobufDeserializer(isKey = false))
      .map { deserializer =>
        Deserializer.instance[F, T] { (topic, _, bytes) =>
          F.pure(deserializer.deserialize(topic, bytes))
        }
      }
}
