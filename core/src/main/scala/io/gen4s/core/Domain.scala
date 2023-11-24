package io.gen4s.core

object Domain {
  case class Topic(value: String)                  extends AnyVal
  case class BootstrapServers(value: String)       extends AnyVal
  case class NumberOfSamplesToGenerate(value: Int) extends AnyVal
}
