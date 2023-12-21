package io.gen4s.benchmarks

import org.openjdk.jmh.annotations.*
import org.openjdk.jmh.infra.Blackhole

import cats.data.NonEmptyList
import io.gen4s.core.generators.*
import io.gen4s.core.templating.{OutputTransformer, SourceTemplate, Template, TemplateBuilder}
import io.gen4s.generators.impl.*

object TemplateRenderBenchmark {

  val name: Variable = Variable("test")

  @State(Scope.Benchmark)
  class BenchmarkState {
    val dateTimeGen: DatetimeGenerator         = DatetimeGenerator(name)
    val timestampGenerator: TimestampGenerator = TimestampGenerator(Variable("ts"))

    val templates: List[Template] = TemplateBuilder
      .make(
        sourceTemplates = NonEmptyList.one(SourceTemplate("""{ "timestamp": {{ts}} }""")),
        generators = List(timestampGenerator),
        globalVariables = List.empty[Variable],
        transformers = Set.empty[OutputTransformer]
      )
      .build()
  }
}

class TemplateRenderBenchmark {

  import TemplateRenderBenchmark.*

  @Benchmark
  def simpleTemplate(state: BenchmarkState, bh: Blackhole): Unit = {
    bh.consume {
      state.templates.foreach(_.render())
    }
  }

}
