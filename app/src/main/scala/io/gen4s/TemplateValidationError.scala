package io.gen4s

import scala.util.control.NoStackTrace

case class TemplateValidationError(error: List[String]) extends NoStackTrace
