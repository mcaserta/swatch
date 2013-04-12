name := "swatch"

organization := "com.mirkocaserta.swatch"

version := "1.0.0-SNAPSHOT"

scalaVersion := "2.10.1"

libraryDependencies ++= Seq(
  "ch.qos.logback" % "logback-classic" % "1.0.11" % "test",
  "com.typesafe.akka" %% "akka-kernel" % "2.1.2",
  "com.typesafe.akka" %% "akka-slf4j" % "2.1.2",
  "com.typesafe.akka" %% "akka-testkit" % "2.1.2" % "test",
  "org.scalatest" %% "scalatest" % "1.9.1" % "test",
  "org.specs2" %% "specs2" % "1.13" % "test"
)

scalacOptions ++= Seq("-encoding", "UTF-8", "-deprecation", "-unchecked", "-feature")