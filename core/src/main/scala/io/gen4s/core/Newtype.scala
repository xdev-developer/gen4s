package io.gen4s.core

import cats.{Eq, Order, Show}
import io.circe.{Decoder, Encoder}

import monocle.Iso

abstract class Newtype[A](using
  eqv: Eq[A],
  ord: Order[A],
  shw: Show[A],
  enc: Encoder[A],
  dec: Decoder[A]
) {

  opaque type Type = A

  inline def apply(a: A): Type            = a
  extension (t: Type) inline def value: A = t

  given Wrapper[A, Type] with {
    def iso: Iso[A, Type] = Iso[A, Type](apply(_))(_.value)
  }

  given Eq[Type]       = eqv
  given Order[Type]    = ord
  given Show[Type]     = shw
  given Encoder[Type]  = enc
  given Decoder[Type]  = dec
  given Ordering[Type] = ord.toOrdering
}
