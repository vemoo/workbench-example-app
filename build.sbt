enablePlugins(ScalaJSPlugin, WorkbenchPlugin)

scalaVersion in ThisBuild := "2.12.2"

name := "scalajs-react TodoMVC example"

version := "0.1-SNAPSHOT"

libraryDependencies ++= Seq(
  "org.scala-js" %%% "scalajs-dom" % "0.9.1",
  "com.github.japgolly.scalajs-react" %%% "core" % "1.0.0",
  "com.github.japgolly.scalajs-react" %%% "extra" % "1.0.0",
  "com.beachape" %%% "enumeratum" % "1.5.12",
  "io.monix" %%% "monix" % "2.2.4"
)

scalacOptions ++= Seq("-unchecked", "-deprecation", "-feature", "-Xfatal-warnings")