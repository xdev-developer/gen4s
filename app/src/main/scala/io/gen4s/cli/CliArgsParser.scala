package io.gen4s.cli

import java.io.File

import io.gen4s.conf.ExecMode
import io.gen4s.core.Domain.*

class CliArgsParser extends scopt.OptionParser[Args]("gen4s") {
  head("Gen4s")

  opt[File]('c', "config")
    .required()
    .withFallback(() => new File("./config.conf"))
    .valueName("<file>")
    .action((x, c) => c.copy(configFile = x))
    .text("Configuration file. Default ./config.conf")
    .validate(x =>
      if (x.exists()) success
      else failure(s"Config file ${x.getAbsolutePath} doesn't exist.")
    )

  opt[File]('p', "profile")
    .valueName("<file>")
    .action((x, c) => c.copy(profileFile = Option(x).filter(_.exists())))
    .text("Environment variables profile.")

  cmd("preview")
    .action((_, c) => c.copy(mode = ExecMode.Preview))
    .text("Preview data generation.")
    .children(
      opt[Unit]("pretty")
        .action((x, c) => c.copy(prettyPreview = true))
        .text("pretty print"),
      opt[Int]('s', "samples")
        .required()
        .withFallback(() => 1)
        .action((x, c) => c.copy(numberOfSamplesToGenerate = NumberOfSamplesToGenerate(x)))
        .text("Samples to generate, default 1")
        .valueName("<number>")
        .validate(x =>
          if (x > 0 && x <= 1000_000) success
          else failure("Option --samples must be > 0 < 1M")
        )
    )

  cmd("run")
    .action((_, c) => c.copy(mode = ExecMode.Run))
    .text("Run data generation stream.")
    .children(
      opt[Int]('s', "samples")
        .required()
        .withFallback(() => 1)
        .action((x, c) => c.copy(numberOfSamplesToGenerate = NumberOfSamplesToGenerate(x)))
        .text("Samples to generate, default 1")
        .valueName("<number>")
        .validate(x =>
          if (x > 0) success
          else failure("Option --samples must be > 0")
        )
    )

  cmd("scenario")
    .action((_, c) => c.copy(mode = ExecMode.RunScenario))
    .text("Run scenario")

  help("help").text("prints usage info")
}
