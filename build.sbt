enablePlugins(ScalaJSPlugin, WorkbenchPlugin)

scalaVersion in ThisBuild := "2.11.8"

name := "Example"

version := "0.1-SNAPSHOT"

libraryDependencies ++= Seq(
  "org.scala-js" %%% "scalajs-dom" % "0.9.1",
  "com.github.japgolly.scalajs-react" %%% "core" % "1.0.0-RC2",
  "com.github.japgolly.scalajs-react" %%% "extra" % "1.0.0-RC2",
  "com.beachape" %%% "enumeratum" % "1.5.10",
  "io.monix" %%% "monix" % "2.2.3"
)

scalacOptions ++= Seq("-unchecked", "-deprecation", "-feature", "-Xfatal-warnings")