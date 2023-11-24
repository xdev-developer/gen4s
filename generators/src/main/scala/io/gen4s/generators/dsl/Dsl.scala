package io.gen4s.generators
package dsl

enum Dsl:
  case PureValue(value: String)                   extends Dsl
  case AnySymbols(minLength: Int, maxLength: Int) extends Dsl
  case Word(minLength: Int, maxLength: Int)       extends Dsl
  case Number(minValue: Int, maxValue: Int)       extends Dsl
  case HEX(minLength: Int, maxLength: Int)        extends Dsl
  case IPv4                                       extends Dsl
  case IPv6                                       extends Dsl
  case MacAddress                                 extends Dsl
