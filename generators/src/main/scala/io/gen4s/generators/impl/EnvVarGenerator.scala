package io.gen4s.generators.impl

import io.circe.derivation.ConfiguredCodec
import io.circe.refined.*
import io.gen4s.core.generators.*
import io.gen4s.generators.codec.given
import io.gen4s.generators.impl.EnvVarGenerator.{ALLOWED_VAR_PREFIX, allowedVars}

import eu.timepit.refined.types.string.NonEmptyString

object EnvVarGenerator {

  private val allowedVars =
    List(
      "CUSTOMER_ID",
      "USER_ID",
      "USERNAME",
      "ORG_ID",
      "EVENT_ID",
      "user.name",
      "os.name"
    )

  private val ALLOWED_VAR_PREFIX = "G4S_"
}

final case class EnvVarGenerator(variable: Variable, name: NonEmptyString, default: Option[String]) extends Generator
    derives ConfiguredCodec:

  override def gen(): GeneratedValue = {
    val varName = name.value
    if (allowedVars.contains(varName) || varName.startsWith(ALLOWED_VAR_PREFIX)) {
      GeneratedValue.fromString {
        sys.env
          .get(varName)
          .orElse(sys.props.get(varName))
          .orElse(default)
          .getOrElse(varName)
      }
    } else {
      GeneratedValue.fromString(default.getOrElse(varName))
    }
  }
