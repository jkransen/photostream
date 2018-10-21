import Dependencies._

val circeVersion = "0.9.3"

lazy val root = (project in file(".")).
  settings(
    inThisBuild(List(
      organization := "nl.kransen",
      scalaVersion := "2.12.7",
      version      := "0.1.0-SNAPSHOT"
    )),
    name := "Photostream",
    libraryDependencies ++= Seq(
      akkaStream,
      akkaHttp,
      slf4j,
      "io.circe"               %% "circe-core"        % circeVersion,
      "io.circe"               %% "circe-generic"     % circeVersion,
      "io.circe"               %% "circe-parser"      % circeVersion,
      "de.heikoseeberger"      %% "akka-http-circe"   % "1.22.0",

      // akkaStreamContrib,
      scalaTest % Test,
      akkaStreamTest % Test
    )
  )
