package io.gen4s

import scala.util.control.NoStackTrace

final case class TemplateValidationError(error: List[String]) extends NoStackTrace
