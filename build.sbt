// Turn this project into a Scala.js project by importing these settings
import com.lihaoyi.workbench.Plugin._

enablePlugins(ScalaJSPlugin)

workbenchSettings

scalaVersion in ThisBuild := "2.12.0"

name := "Example"

version := "0.1-SNAPSHOT"

libraryDependencies ++= Seq(
  "org.scala-js" %%% "scalajs-dom" % "0.9.1",
  "com.github.japgolly.scalajs-react" %%% "core" % "0.11.3",
  "com.github.japgolly.scalajs-react" %%% "extra" % "0.11.3",
  "com.beachape" %%% "enumeratum" % "1.5.1"
  ,"com.github.chandu0101" %%% "sri-web" % "0.6.0"
)

scalacOptions ++= Seq("-unchecked", "-deprecation", "-feature", "-Xfatal-warnings")

bootSnippet := "todomvc.JSMain().main()"

updateBrowsers <<= updateBrowsers.triggeredBy(fastOptJS in Compile)