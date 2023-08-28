val Scala3 = "3.3.0"

ThisBuild / scalaVersion := Scala3
ThisBuild / version      := "0.0.1"

ThisBuild / scalafmtOnCompile := true

ThisBuild / scalacOptions += "-Wunused:all"

resolvers ++= Resolver.sonatypeOssRepos("snapshots")

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
      Dependencies.ApacheCommonsText,
      Dependencies.CatsEffectTest,
      Dependencies.ScalaTest
    )
  )

lazy val outputs = project
  .in(file("outputs"))
  .settings(
    name := "gen4s-outputs",
    libraryDependencies ++= List.concat(
      Dependencies.Cats,
      Dependencies.CatsEffect,
      Dependencies.Circe,
      Dependencies.Fs2,
      Dependencies.Fs2Kafka,
      Dependencies.Enumeratum,
      Dependencies.Refined,
      Dependencies.Logback,
      Dependencies.CatsEffectTest,
      Dependencies.TestContainers,
      Dependencies.ScalaTest
    )
  )
  .dependsOn(core)

lazy val app = project
  .in(file("app"))
  .settings(
    name := "gen4s-app",
    libraryDependencies ++= List.concat(
      Dependencies.Scopt,
      Dependencies.Refined,
      Dependencies.CatsEffect,
      Dependencies.Pureconfig,
      Dependencies.Log4cats,
      Dependencies.Logback,
      Dependencies.ScalaTest,
      Dependencies.CatsEffectTest
    )
  )
  .dependsOn(core, outputs)

lazy val root = project
  .in(file("."))
  .aggregate(core, outputs, app)
  .settings(
    name := "gen4s"
  )
