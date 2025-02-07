package io.gen4s.core.templating

import cats.data.Validated
import cats.syntax.all.*

import enumeratum.*

sealed abstract class OutputValidator(override val entryName: String) extends EnumEntry {
  def validate(template: RenderedTemplate): Validated[String, Unit]
}

object OutputValidator extends Enum[OutputValidator] with CirceEnum[OutputValidator] {

  val values: IndexedSeq[OutputValidator] = findValues

  case object JSON extends OutputValidator("json") {

    override def validate(template: RenderedTemplate): Validated[String, Unit] = {
      template.asJson.toValidated
        .leftMap(err => s"JSON Validation error: ${err.message}")
        .map(_ => ())
    }
  }

  case object MissingVars extends OutputValidator("missing-vars") {
    private val varPattern = "\\$\\{[\\w.-]+}".r

    override def validate(template: RenderedTemplate): Validated[String, Unit] = {
      varPattern
        .findFirstIn(template.asString)
        .map(v => s"Found unresolved variable: $v".invalid[Unit])
        .getOrElse(().valid[String])
    }
  }

  case object OldStyleVars extends OutputValidator("old-style-vars") {
    private val varPattern = "\\{\\{[\\w.-]+}}".r

    override def validate(template: RenderedTemplate): Validated[String, Unit] = {
      varPattern
        .findFirstIn(template.asString)
        .map(v =>
          s"Found old style variable: $v, please migrate your template to use new $${variable-name} variable substitution format."
            .invalid[Unit]
        )
        .getOrElse(().valid[String])
    }
  }
}
