package io.gen4s.generators.dsl

import scala.util.parsing.combinator.*

@SuppressWarnings(Array("org.wartremover.warts.Throw"))
object TemplateParser extends RegexParsers:

  override val skipWhitespace: Boolean = false

  private val number = "\\d+".r ^^ { _.toInt }

  private val lengthParser: Parser[(Int, Int)] = "{" ~ (number <~ ("," ~ " ".?)).? ~ number ~ "}" ^^ {
    case _ ~ minLength ~ maxLength ~ _ =>
      val mLength = minLength.getOrElse(maxLength)
      if (mLength > maxLength) {
        throw new Exception("minLength must be less or equal maxLemgth")
      } else {
        (mLength, maxLength)
      }
  }

  private val word: Parser[Dsl] = "%s" ~ lengthParser ^^ { case _ ~ (minLength, maxLength) =>
    Dsl.Word(minLength, maxLength)
  }

  private val anySymbols: Parser[Dsl] = "*" ~ lengthParser.? ^^ { case _ ~ length =>
    val (minLength, maxLength) = length.getOrElse((1, 1))
    Dsl.AnySymbols(minLength, maxLength)
  }

  private val numberParser: Parser[Dsl] = "%n" ~ lengthParser ^^ { case _ ~ (minValue, maxValue) =>
    Dsl.Number(minValue, maxValue)
  }

  private val hexPaser: Parser[Dsl] = "#" ~ lengthParser ^^ { case _ ~ (minLength, maxLength) =>
    Dsl.HEX(minLength, maxLength)
  }

  private val ipv4: Parser[Dsl] = "%ip4" ^^^ Dsl.IPv4

  private val ipv6: Parser[Dsl] = "%ip6" ^^^ Dsl.IPv6

  private val mac: Parser[Dsl] = "%mac" ^^^ Dsl.MacAddress

  private val whitespaceParser: Parser[Dsl] = whiteSpace ^^ (v => Dsl.PureValue(v))

  private val pureParser: Parser[Dsl] =
    """[\p{Alnum}!\"$&'()+,-.\/:;<=>?\\@\[\]^_`{|}~]+""".r ^^ (v => Dsl.PureValue(v))

  private val templateParser: Parser[Seq[Dsl]] = rep(
    anySymbols | word | numberParser | hexPaser | ipv4 | ipv6 | mac | whitespaceParser | pureParser
  )

  def process(input: String) =
    parse(templateParser, input) match
      case Success(matched, _) => matched
      case Failure(msg, _)     => throw new Exception(msg)
      case Error(msg, _)       => throw new Exception(msg)
