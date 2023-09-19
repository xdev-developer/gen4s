package io.gen4s.generators.render

import io.gen4s.generators.dsl.Dsl

trait Renderrer[-T]:
  def generate(obj: T): String

object Renderrer:

  given Renderrer[Dsl.PureValue] = new:
    def generate(obj: Dsl.PureValue): String = obj.value

  given Renderrer[Dsl.AnySymbols] = new:
    def generate(obj: Dsl.AnySymbols): String = RandomAnySymbols.generate(obj.minLength, obj.maxLength)

  given Renderrer[Dsl.Word] = new:
    def generate(obj: Dsl.Word): String = RandomWord.generate(obj.minLength, obj.maxLength)

  given Renderrer[Dsl.Number] = new:
    def generate(obj: Dsl.Number): String = RandomNumber.generate(obj.minValue, obj.maxValue).toString

  given Renderrer[Dsl.HEX] = new:
    def generate(obj: Dsl.HEX): String = RandomHEX.generate(obj.minLength, obj.maxLength)

  given Renderrer[Dsl.IPv4.type] = new:
    def generate(obj: Dsl.IPv4.type): String = RandomIPv4.generate(0, 0)

  given Renderrer[Dsl.IPv6.type] = new:
    def generate(obj: Dsl.IPv6.type): String = RandomIPv6.generate(0, 0)

  given Renderrer[Dsl.MacAddress.type] = new:
    def generate(obj: Dsl.MacAddress.type): String = RandomMacAddress.generate(0, 0)

  given Renderrer[Dsl] = new:

    def generate(obj: Dsl): String = obj match
      case o: Dsl.PureValue       => implicitly[Renderrer[Dsl.PureValue]].generate(o)
      case o: Dsl.AnySymbols      => implicitly[Renderrer[Dsl.AnySymbols]].generate(o)
      case o: Dsl.Word            => implicitly[Renderrer[Dsl.Word]].generate(o)
      case o: Dsl.Number          => implicitly[Renderrer[Dsl.Number]].generate(o)
      case o: Dsl.HEX             => implicitly[Renderrer[Dsl.HEX]].generate(o)
      case o: Dsl.IPv4.type       => implicitly[Renderrer[Dsl.IPv4.type]].generate(o)
      case o: Dsl.IPv6.type       => implicitly[Renderrer[Dsl.IPv6.type]].generate(o)
      case o: Dsl.MacAddress.type => implicitly[Renderrer[Dsl.MacAddress.type]].generate(o)
