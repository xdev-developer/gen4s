package io.gen4s.core.generators

import io.gen4s.core.Newtype

/**
 * Represents template variable reference - generator for what variable in template
 */
type Variable = Variable.Type
object Variable extends Newtype[String]
