package io.gen4s.cli

import java.io.File

import io.gen4s.conf.ExecMode
import io.gen4s.core.Domain.*

class CliArgsParser extends scopt.OptionParser[Args]("gen4s") {
  head("Gen4s", "0.0.1")

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
}
