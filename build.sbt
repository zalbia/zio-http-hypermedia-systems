import BuildHelper.*
import Libraries.*

Global / onChangedBuildSource := ReloadOnSourceChanges

ThisBuild / scalaVersion               := "2.13.12"
ThisBuild / version                    := "0.1.0-SNAPSHOT"
ThisBuild / organization               := "com.github.zalbia"
ThisBuild / scalafmtCheck              := true
ThisBuild / scalafmtSbtCheck           := true
ThisBuild / semanticdbEnabled          := true
ThisBuild / semanticdbOptions += "-P:semanticdb:synthetics:on"
ThisBuild / semanticdbVersion          := scalafixSemanticdb.revision // use Scalafix compatible version
ThisBuild / scalafixScalaBinaryVersion := CrossVersion.binaryScalaVersion(scalaVersion.value)
ThisBuild / scalafixDependencies ++= List(
  "com.github.vovapolu"                      %% "scaluzzi" % "0.1.23",
  "io.github.ghostbuster91.scalafix-unified" %% "unified"  % "0.0.9",
)

addCommandAlias("fmt", "scalafmtAll; scalafmtSbt; scalafix")

// copied from @guizmaii
addCommandAlias("tc", "Test/compile")
addCommandAlias("ctc", "clean; Test/compile")
addCommandAlias("rctc", "reload; clean; Test/compile")
addCommandAlias("start", "~root/reStart")
addCommandAlias("stop", "reStop")
addCommandAlias("restart", "stop;start")
addCommandAlias("rst", "restart")

lazy val root =
  (project in file("."))
    .enablePlugins(SbtTwirl)
    .settings(stdSettings*)
    .settings(Revolver.enableDebugging())
    .settings(reLogTag := "zio-http-hypermedia-systems")
    .settings(
      name := "zio-http-hypermedia-systems",
      libraryDependencies ++= Seq(zioHttp) ++ loggingRuntime,
      testFrameworks += new TestFramework("zio.test.sbt.ZTestFramework"),
    )
