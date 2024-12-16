import sbt.*

object Dependencies {

  def circe(artifact: String): ModuleID =
    "io.circe" %% s"circe-$artifact" % V.circe

  object V {
    val cats              = "2.12.0"
    val catsEffect        = "3.5.7"
    val catsRetry         = "3.1.0"
    val circe             = "0.14.9"
    val fs2               = "3.11.0"
    val fs2Kafka          = "3.5.1"
    val log4cats          = "2.7.0"
    val pureConfig        = "0.17.8"
    val sttp              = "3.10.1"
    var refined           = "0.11.2"
    val parserCombinators = "2.4.0"
    val csv               = "1.4.1"
    val monocle           = "3.3.0"

    val scalaTest      = "3.2.19"
    val testContainers = "0.41.4"

    val betterMonadicFor = "0.3.1"
    val kindProjector    = "0.13.2"
    val logback          = "1.5.12"
  }

  val Cats: Seq[ModuleID] = List(
    "org.typelevel" %% "cats-core" % V.cats
  )

  val CatsEffect: Seq[ModuleID] = List(
    "org.typelevel" %% "cats-effect" % V.catsEffect
  )

  val ApacheCommonsText: Seq[ModuleID] = List(
    "org.apache.commons" % "commons-text" % "1.13.0"
  )

  val ApacheCommons: Seq[ModuleID] = List(
    "org.apache.commons" % "commons-text"  % "1.13.0",
    "commons-codec"      % "commons-codec" % "1.17.1",
    "org.xerial.snappy"  % "snappy-java"   % "1.1.10.7",
    "commons-io"         % "commons-io"    % "2.18.0",
    "org.xerial.snappy"  % "snappy-java"   % "1.1.10.7"
  )

  val ScalaCsv: Seq[ModuleID] = List(
    "com.github.tototoshi" %% "scala-csv" % V.csv
  )

  val Fs2: Seq[ModuleID] = List(
    "co.fs2" %% "fs2-core" % V.fs2
  )

  val Fs2Io: Seq[ModuleID] = List(
    "co.fs2" %% "fs2-io" % V.fs2
  )

  val Fs2Kafka: Seq[ModuleID] = List(
    "com.github.fd4s" %% "fs2-kafka"        % V.fs2Kafka,
    "com.github.fd4s" %% "fs2-kafka-vulcan" % V.fs2Kafka
  )

  val Fs2S3: Seq[ModuleID] = List(
    "io.laserdisc" %% "fs2-aws-s3" % "6.1.3"
  )

  val CirceCore    = circe("core")
  val CirceParser  = circe("parser")
  val CirceRefined = circe("refined")

  val Enumeratum: Seq[ModuleID] = List(
    "com.beachape" %% "enumeratum"       % "1.7.5",
    "com.beachape" %% "enumeratum-circe" % "1.7.5"
  )

  val Log4cats: Seq[ModuleID] = List(
    "org.typelevel" %% "log4cats-slf4j" % V.log4cats
  )

  val Refined: Seq[ModuleID] = List(
    "eu.timepit" %% "refined"      % V.refined,
    "eu.timepit" %% "refined-cats" % V.refined
  )

  val Pureconfig: Seq[ModuleID] = List(
    "com.github.pureconfig" %% "pureconfig-core"        % V.pureConfig,
    "com.github.pureconfig" %% "pureconfig-cats-effect" % V.pureConfig,
    "com.github.pureconfig" %% "pureconfig-enumeratum"  % V.pureConfig,
    "com.github.pureconfig" %% "pureconfig-cats"        % V.pureConfig,
    "eu.timepit"            %% "refined-pureconfig"     % V.refined
  )

  val Scopt: Seq[ModuleID] = List("com.github.scopt" %% "scopt" % "4.1.0")

  val FS2Throttler: Seq[ModuleID] = List(
    "dev.kovstas" %% "fs2-throttler" % "1.0.8"
  )

  val Sttp: Seq[ModuleID] = List(
    "com.softwaremill.sttp.client3" %% "core"                           % V.sttp,
    "com.softwaremill.sttp.client3" %% "async-http-client-backend-cats" % V.sttp
  )

  val ParserCombinators: Seq[ModuleID] = Seq(
    "org.scala-lang.modules" %% "scala-parser-combinators" % V.parserCombinators
  )

  val Circe: Seq[ModuleID] = List(
    CirceCore,
    CirceParser,
    CirceRefined
  )

  val AvroConverter: Seq[ModuleID] = List(
    "tech.allegro.schema.json2avro" % "converter" % "0.2.15"
  )

  val Monocle: Seq[ModuleID] = List(
    "dev.optics" %% "monocle-core" % V.monocle
  )

  val ProgressBar: Seq[ModuleID] = List(
    "me.tongfei" % "progressbar" % "0.10.1"
  )

  val ProtoConverter: Seq[ModuleID] = List(
    "io.confluent"        % "kafka-protobuf-serializer" % "7.7.0",
    "com.google.protobuf" % "protobuf-java-util"        % "3.25.5"
  )

  val ScalaTest: Seq[ModuleID]      = List("org.scalatest" %% "scalatest" % V.scalaTest % Test)
  val CatsEffectTest: Seq[ModuleID] = List("org.typelevel" %% "cats-effect-testing-scalatest" % "1.5.0" % Test)

  val TestContainers: Seq[ModuleID] = List(
    "com.dimafeng" %% "testcontainers-scala-scalatest"     % V.testContainers % Test,
    "com.dimafeng" %% "testcontainers-scala-kafka"         % V.testContainers % Test,
    "com.dimafeng" %% "testcontainers-scala-localstack-v2" % V.testContainers % Test
  )

  // Runtime
  val Logback: Seq[ModuleID] = List(
    "ch.qos.logback" % "logback-classic"  % V.logback,
    "org.slf4j"      % "log4j-over-slf4j" % "2.0.16"
  )
}
