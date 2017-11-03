import ReleaseTransformations._
import Dependencies._

name := "identity-play-auth"

organization := "com.gu.identity"

scalaVersion := "2.11.11"

crossScalaVersions := Seq(scalaVersion.value)

scmInfo := Some(ScmInfo(
  url("https://github.com/guardian/identity-play-auth"),
  "scm:git:git@github.com:guardian/identity-play-auth.git"
))

description := "A small client library for Guardian Identity authentication with the Play framework"

licenses := Seq("Apache V2" -> url("http://www.apache.org/licenses/LICENSE-2.0.html"))

updateOptions := updateOptions.value.withCachedResolution(true)

resolvers += Resolver.sonatypeRepo("releases")

libraryDependencies ++= Seq(
  play,
  identityCookie,
  identityTestUsers,
  scalaTestPlus % Test,
  scalactic % Test
)

lazy val root = project in file(".")

releaseProcess := Seq[ReleaseStep](
  checkSnapshotDependencies,
  inquireVersions,
  runClean,
  runTest,
  setReleaseVersion,
  commitReleaseVersion,
  tagRelease,
  releaseStepCommand("publishSigned"),
  setNextVersion,
  commitNextVersion,
  releaseStepCommand("sonatypeReleaseAll"),
  pushChanges
)
