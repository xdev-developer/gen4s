package io.gen4s.generators.render

import io.gen4s.generators.dsl.Dsl

trait Renderer[-T]:
  def generate(obj: T): String

object Renderer:

  given Renderer[Dsl.PureValue] = new:
    def generate(obj: Dsl.PureValue): String = obj.value

  given Renderer[Dsl.AnySymbols] = new:
    def generate(obj: Dsl.AnySymbols): String = RandomAnySymbols.generate(obj.minLength, obj.maxLength)

  given Renderer[Dsl.Word] = new:
    def generate(obj: Dsl.Word): String = RandomWord.generate(obj.minLength, obj.maxLength)

  given Renderer[Dsl.Number] = new:
    def generate(obj: Dsl.Number): String = RandomNumber.generate(obj.minValue, obj.maxValue).toString

  given Renderer[Dsl.HEX] = new:
    def generate(obj: Dsl.HEX): String = RandomHEX.generate(obj.minLength, obj.maxLength)

  given Renderer[Dsl.IPv4.type] = new:
    def generate(obj: Dsl.IPv4.type): String = RandomIPv4.generate(0, 0)

  given Renderer[Dsl.IPv6.type] = new:
    def generate(obj: Dsl.IPv6.type): String = RandomIPv6.generate(0, 0)

  given Renderer[Dsl.MacAddress.type] = new:
    def generate(obj: Dsl.MacAddress.type): String = RandomMacAddress.generate(0, 0)

  given Renderer[Dsl] = new:

    def generate(obj: Dsl): String = obj match
      case o: Dsl.PureValue       => implicitly[Renderer[Dsl.PureValue]].generate(o)
      case o: Dsl.AnySymbols      => implicitly[Renderer[Dsl.AnySymbols]].generate(o)
      case o: Dsl.Word            => implicitly[Renderer[Dsl.Word]].generate(o)
      case o: Dsl.Number          => implicitly[Renderer[Dsl.Number]].generate(o)
      case o: Dsl.HEX             => implicitly[Renderer[Dsl.HEX]].generate(o)
      case o: Dsl.IPv4.type       => implicitly[Renderer[Dsl.IPv4.type]].generate(o)
      case o: Dsl.IPv6.type       => implicitly[Renderer[Dsl.IPv6.type]].generate(o)
      case o: Dsl.MacAddress.type => implicitly[Renderer[Dsl.MacAddress.type]].generate(o)
