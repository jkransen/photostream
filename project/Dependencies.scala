import sbt._

object Dependencies {
  lazy val scalaTest         = "org.scalatest"     %% "scalatest"           % "3.0.5"
  lazy val akkaStream        = "com.typesafe.akka" %% "akka-stream"         % "2.5.16"
  lazy val akkaHttp          = "com.typesafe.akka" %% "akka-http"           % "10.1.3"
  lazy val akkaStreamContrib = "com.typesafe.akka" %% "akka-stream-contrib" % "0.9"
  lazy val akkaStreamTest    = "com.typesafe.akka" %% "akka-stream-testkit" % "2.5.16"
  lazy val slf4j             = "org.slf4j"         % "slf4j-log4j12"        % "1.7.25"
}
