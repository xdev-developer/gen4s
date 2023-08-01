import Dependencies.CompilerPlugin

val Scala213 = "2.13.11"
val Scala3 = "3.3.0"

ThisBuild / scalaVersion := Scala3
ThisBuild / version := "0.0.1"

ThisBuild / scalafmtOnCompile := true

resolvers ++= Resolver.sonatypeOssRepos("snapshots")

lazy val core = project.in(file("core"))
  .settings(
    name := "gen4s-core",
    libraryDependencies ++= List.concat(
      Dependencies.cats
    )
  )

lazy val app = project.in(file("app"))
  .settings(
    name := "gen4s-app",
    libraryDependencies ++= List.concat(
      Dependencies.catsEffect,
      Dependencies.fs2,
      Dependencies.pureconfig,
      Dependencies.log4cats,
      Dependencies.logback
    )
  ).dependsOn(core)

lazy val root = project
  .in(file("."))
  .aggregate(core, app)
  .settings(
    name := "gen4s"
  )