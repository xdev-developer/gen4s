import org.typelevel.scalacoptions.ScalacOptions

import NativePackagerHelper.*
import ReleaseTransformations.*

val Scala3 = "3.6.4"

ThisBuild / scalaVersion := Scala3

ThisBuild / scalafmtOnCompile := true

ThisBuild / scalacOptions += "-Wunused:all"
ThisBuild / scalacOptions ++= Seq("-Xmax-inlines", "50")
ThisBuild / scalacOptions ++= Seq("-Wconf:msg=unused value of type org.scalatest.Assertion:s")

ThisBuild / resolvers += Resolver.sonatypeCentralSnapshots
ThisBuild / resolvers += "confluent" at "https://packages.confluent.io/maven/"
ThisBuild / resolvers += "jitpack" at "https://jitpack.io"

ThisBuild / wartremoverErrors ++= Warts.allBut(Wart.Any, Wart.DefaultArguments, Wart.Overloading)

ThisBuild / coverageExcludedPackages := ".*App.*;.*CliArgsParser.*;"

ThisBuild / Test / testOptions += Tests.Argument(TestFrameworks.ScalaTest, "-oSID")

ThisBuild / Test / parallelExecution := false

lazy val core = project
  .in(file("core"))
  .settings(
    name := "gen4s-core",
    libraryDependencies ++= List.concat(
      Dependencies.Cats,
      Dependencies.CatsEffect,
      Dependencies.Circe,
      Dependencies.Fs2,
      Dependencies.Enumeratum,
      Dependencies.Refined,
      Dependencies.Monocle,
      Dependencies.ApacheCommonsText,
      Dependencies.CatsEffectTest,
      Dependencies.ScalaTest
    )
  )

lazy val generators = project
  .in(file("generators"))
  .settings(
    name := "gen4s-generators",
    libraryDependencies ++= List.concat(
      Dependencies.Cats,
      Dependencies.ParserCombinators,
      Dependencies.Ride,
      Dependencies.ScalaTest,
      Dependencies.CatsEffectTest
    ),
    Test / tpolecatExcludeOptions += ScalacOptions.warnNonUnitStatement
  )
  .dependsOn(core)

lazy val outputs = project
  .in(file("outputs"))
  .settings(
    name         := "gen4s-outputs",
    scalaVersion := Scala3,
    libraryDependencies ++= List.concat(
      Dependencies.Cats,
      Dependencies.CatsEffect,
      Dependencies.Circe,
      Dependencies.Fs2,
      Dependencies.Fs2Kafka,
      Dependencies.Fs2Io,
      Dependencies.Fs2S3,
      Dependencies.Sttp,
      Dependencies.ApacheCommons,
      Dependencies.Enumeratum,
      Dependencies.Refined,
      Dependencies.Logback,
      Dependencies.Log4cats,
      Dependencies.AvroConverter,
      Dependencies.ProgressBar,
      Dependencies.ProtoConverter,
      Dependencies.CatsEffectTest,
      Dependencies.TestContainers,
      Dependencies.ScalaTest
    ),
    Test / tpolecatExcludeOptions += ScalacOptions.warnNonUnitStatement,
    Test / PB.protocOptions := Seq(
      "--include_imports",
      "--descriptor_set_out=" + (Test / baseDirectory).value / "src" / "test" / "resources" / "person-value.desc"
    ),
    Test / PB.targets := Seq(
      PB.gens.java -> (Test / sourceManaged).value
    )
  )
  .dependsOn(core, generators)

lazy val benchmarks = project
  .in(file("benchmarks"))
  .dependsOn(generators % "test->test")
  .enablePlugins(JmhPlugin)
  .settings(
    name                      := "gen4s-benchmarks",
    scalaVersion              := Scala3,
    Jmh / sourceDirectory     := (Test / sourceDirectory).value,
    Jmh / classDirectory      := (Test / classDirectory).value,
    Jmh / dependencyClasspath := (Test / dependencyClasspath).value,
    // rewire tasks, so that 'bench/Jmh/run' automatically invokes 'bench/Jmh/compile' (otherwise a clean 'bench/Jmh/run' would fail)
    Jmh / compile := (Jmh / compile).dependsOn(Test / compile).value,
    Jmh / run     := (Jmh / run).dependsOn(Jmh / compile).evaluated
  )

lazy val app = project
  .in(file("app"))
  .enablePlugins(JavaAppPackaging)
  .enablePlugins(BuildInfoPlugin)
  .settings(
    name := "gen4s-app",
    libraryDependencies ++= List.concat(
      Dependencies.Scopt,
      Dependencies.Refined,
      Dependencies.CatsEffect,
      Dependencies.Pureconfig,
      Dependencies.Log4cats,
      Dependencies.Logback,
      Dependencies.ScalaCsv,
      Dependencies.ScalaTest,
      Dependencies.CatsEffectTest
    ),
    Universal / maintainer                                       := "xdev.developer@gmail.com,pavel.tsipinio@gmail.com",
    executableScriptName                                         := "gen4s",
    Universal / packageXzTarball / mappings += file("README.md") -> "README.md",
    Universal / packageXzTarball / mappings ++= directory("examples"),
    Universal / packageBin / mappings := (Universal / packageXzTarball / mappings).value,
    Compile / packageDoc / mappings   := Seq(),
    Test / tpolecatExcludeOptions += ScalacOptions.warnNonUnitStatement,
    buildInfoKeys    := Seq[BuildInfoKey](name, version),
    buildInfoPackage := "io.gen4s.app.build.info"
  )
  .dependsOn(core, generators, outputs)

lazy val root = project
  .in(file("."))
  .aggregate(core, generators, outputs, benchmarks, app)
  .settings(
    name           := "gen4s",
    releaseProcess := Seq[ReleaseStep](
      checkSnapshotDependencies, // : ReleaseStep
      inquireVersions,           // : ReleaseStep
      runClean,                  // : ReleaseStep
      runTest,                   // : ReleaseStep
      setReleaseVersion,         // : ReleaseStep
      commitReleaseVersion,      // : ReleaseStep, performs the initial git checks
      tagRelease,                // : ReleaseStep
      setNextVersion,            // : ReleaseStep
      commitNextVersion          // : ReleaseStep
    ),
    releaseTagName := s"release-v${if (releaseUseGlobalVersion.value) (ThisBuild / version).value else version.value}",
    Test / tpolecatExcludeOptions += ScalacOptions.warnNonUnitStatement
  )
