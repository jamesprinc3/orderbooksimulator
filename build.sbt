import sbt.Keys.libraryDependencies

name := "OrderBookSimulator"

version := "0.1"

scalaVersion := "2.12.4"

libraryDependencies  ++= Seq(
  // Last stable release
  "org.scalanlp" %% "breeze" % "0.13.2",

  // Native libraries are not included by default. add this if you want them (as of 0.7)
  // Native libraries greatly improve performance, but increase jar sizes.
  // It also packages various blas implementations, which have licenses that may or may not
  // be compatible with the Apache License. No GPL code, as best I know.
  "org.scalanlp" %% "breeze-natives" % "0.13.2",

  // The visualization library is distributed separately as well.
  // It depends on LGPL code
  "org.scalanlp" %% "breeze-viz" % "0.13.2",

  "org.scalactic" %% "scalactic" % "3.0.4",

  "com.github.tototoshi" %% "scala-csv" % "1.3.5",
  "ch.qos.logback" % "logback-classic" % "1.2.3",
  "com.typesafe.scala-logging" %% "scala-logging" % "3.9.0",
  "com.github.scopt" %% "scopt" % "3.7.0",
  "io.spray" %%  "spray-json" % "1.3.3",
  "com.typesafe" % "config" % "1.3.2",

  "org.scalatest" %% "scalatest" % "3.0.4" % Test,
  "org.scalamock" %% "scalamock" % "4.1.0" % Test
)

resolvers += "Sonatype Releases" at "https://oss.sonatype.org/content/repositories/releases/"
