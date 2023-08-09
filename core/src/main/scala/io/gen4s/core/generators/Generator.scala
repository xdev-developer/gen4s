package io.gen4s.core.generators

object Generator {}

trait Generator {
  def gen(): GeneratedValue
}
