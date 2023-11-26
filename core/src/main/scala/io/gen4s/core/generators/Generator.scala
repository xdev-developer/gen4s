package io.gen4s.core.generators

trait Generator {
  val variable: Variable
  def gen(): GeneratedValue
}
