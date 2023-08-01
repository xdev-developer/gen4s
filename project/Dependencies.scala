import sbt._

object Dependencies {

  def circe(artifact: String): ModuleID  = "io.circe"   %% s"circe-$artifact"  % V.circe

  object V {
    val cats          = "2.9.0"
    val catsEffect    = "3.4.11"
    val catsRetry     = "3.1.0"
    val circe         = "0.14.1"
    val fs2           = "3.7.0"
    val log4cats      = "2.5.0"
    val refined       = "0.10.3"

    val betterMonadicFor = "0.3.1"
    val kindProjector    = "0.13.2"
    val logback          = "1.4.7"
  }

  val cats = List(
    "org.typelevel"    %% "cats-core"   % V.cats
  )

  val catsEffect = List(
    "org.typelevel"    %% "cats-effect" % V.catsEffect
  )

  val fs2 = List(
    "co.fs2"           %% "fs2-core"    % V.fs2
  )

  val circeCore    = circe("core")
  val circeGeneric = circe("generic")
  val circeParser  = circe("parser")
  val circeRefined = circe("refined")

  val refinedCore = "eu.timepit" %% "refined"      % V.refined
  val refinedCats = "eu.timepit" %% "refined-cats" % V.refined

  val log4cats = List("org.typelevel" %% "log4cats-slf4j" % V.log4cats)

  val pureconfig = List(
    "com.github.pureconfig" %% "pureconfig-core" % "0.17.4"
  )

  // Runtime
  val logback = List("ch.qos.logback" % "logback-classic" % V.logback)

  object CompilerPlugin {
    val betterMonadicFor = compilerPlugin(
      "com.olegpy" %% "better-monadic-for" % V.betterMonadicFor
    )
    val kindProjector = compilerPlugin(
      "org.typelevel" % "kind-projector" % V.kindProjector cross CrossVersion.full
    )
  }
}
