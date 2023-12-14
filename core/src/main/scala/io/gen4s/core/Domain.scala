package io.gen4s.core

object Domain {
  opaque type Topic                     = String
  opaque type BootstrapServers          = String
  opaque type NumberOfSamplesToGenerate = Int

  object Topic {
    def apply(value: String): Topic        = value
    extension (v: Topic) def value: String = v
  }

  object BootstrapServers {
    def apply(value: String): BootstrapServers        = value
    extension (v: BootstrapServers) def value: String = v
  }

  object NumberOfSamplesToGenerate {
    def apply(n: Int): NumberOfSamplesToGenerate            = n
    extension (v: NumberOfSamplesToGenerate) def value: Int = v
  }
}
