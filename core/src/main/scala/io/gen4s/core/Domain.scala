package io.gen4s.core

object Domain {

  type Topic = Topic.Type
  object Topic extends Newtype[String]

  type BootstrapServers = BootstrapServers.Type
  object BootstrapServers extends Newtype[String]

  type NumberOfSamplesToGenerate = NumberOfSamplesToGenerate.Type
  object NumberOfSamplesToGenerate extends Newtype[Int]
}
