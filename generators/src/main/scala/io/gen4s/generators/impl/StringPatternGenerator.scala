package io.gen4s.generators.impl

import scala.util.Random

import io.circe.derivation.ConfiguredCodec
import io.circe.refined.*
import io.gen4s.core.generators.{*, given}
import io.gen4s.generators.codec.given

import eu.timepit.refined.types.string.NonEmptyString

object StringPatternGenerator {
  private val chars: Seq[Char] = ('a' to 'z') ++ ('A' to 'Z')
  private val CharsLen: Int    = chars.length
}

final case class StringPatternGenerator(variable: Variable, pattern: NonEmptyString) extends Generator
    derives ConfiguredCodec:

  override def gen(): GeneratedValue = {
    val random = new Random()
    GeneratedValue.fromString(randNumbers(random, randChars(random, pattern.value)))
  }

  private def randNumbers(random: Random, pattern: String): String = {
    pattern.map {
      case '#'  => random.nextInt(10).toString
      case char => char
    }.mkString
  }

  private def randChars(random: Random, pattern: String): String = {
    import StringPatternGenerator.*

    pattern.map {
      case '?'  => chars(random.nextInt(CharsLen))
      case char => char
    }.mkString
  }
