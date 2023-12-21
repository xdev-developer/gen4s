package io.gen4s.benchmarks

import org.openjdk.jmh.annotations.*
import org.openjdk.jmh.infra.Blackhole

import io.gen4s.core.generators.*
import io.gen4s.generators.impl.*

import eu.timepit.refined.types.string.NonEmptyString

object GeneratorsBenchmark {

  val name: Variable = Variable("test")

  @State(Scope.Benchmark)
  class BenchmarkState {
    val timestampGenerator: TimestampGenerator = TimestampGenerator(name)
    val dateTimeGen: DatetimeGenerator         = DatetimeGenerator(name)

    val doubleNumberGenerator: DoubleNumberGenerator = DoubleNumberGenerator(name)
    val intNumberGenerator: IntNumberGenerator       = IntNumberGenerator(name)

    val stringPatternGenerator: StringPatternGenerator =
      StringPatternGenerator(name, NonEmptyString.unsafeFrom("###-???"))
  }
}

class GeneratorsBenchmark {

  import GeneratorsBenchmark.*

  @Benchmark
  def timestampGenerator(state: BenchmarkState, bh: Blackhole): Unit = {
    bh.consume {
      state.timestampGenerator.gen()
    }
  }

  @Benchmark
  def dateTimeGenerator(state: BenchmarkState, bh: Blackhole): Unit = {
    bh.consume {
      state.dateTimeGen.gen()
    }
  }

  @Benchmark
  def doubleNumberGenerator(state: BenchmarkState, bh: Blackhole): Unit = {
    bh.consume {
      state.doubleNumberGenerator.gen()
    }
  }

  @Benchmark
  def intNumberGenerator(state: BenchmarkState, bh: Blackhole): Unit = {
    bh.consume {
      state.intNumberGenerator.gen()
    }
  }

  @Benchmark
  def stringPatternGenerator(state: BenchmarkState, bh: Blackhole): Unit = {
    bh.consume {
      state.stringPatternGenerator.gen()
    }
  }

}
