// Turn this project into a Scala.js project by importing these settings
import com.lihaoyi.workbench.Plugin._

enablePlugins(ScalaJSPlugin)

workbenchSettings

scalaVersion := "2.11.8"

name := "Example"

version := "0.1-SNAPSHOT"

libraryDependencies ++= Seq(
  "org.scala-js" %%% "scalajs-dom" % "0.9.0",
  "com.github.japgolly.scalajs-react" %%% "core" % "0.11.2",
  "com.github.japgolly.scalajs-react" %%% "extra" % "0.11.2",
  "com.beachape" %%% "enumeratum" % "1.4.15"
  ,"com.github.chandu0101" %%% "sri-web" % "0.6.0"
)

scalacOptions ++= Seq("-unchecked", "-deprecation", "-feature", "-Xfatal-warnings")

bootSnippet := "todomvc.sri.JSMain().main()"

updateBrowsers <<= updateBrowsers.triggeredBy(fastOptJS in Compile)


//lazy val react = project
//  .settings(libraryDependencies ++= Seq(
//    "com.github.japgolly.scalajs-react" %%% "core" % "0.11.2",
//    "com.github.japgolly.scalajs-react" %%% "extra" % "0.11.2"
//  ))