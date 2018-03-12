import ReleaseTransformations._
import Dependencies._

name := "identity-play-auth"

organization := "com.gu.identity"

scalaVersion := "2.11.11"

crossScalaVersions := Seq(scalaVersion.value, "2.12.4")

scmInfo := Some(ScmInfo(
  url("https://github.com/guardian/identity-play-auth"),
  "scm:git:git@github.com:guardian/identity-play-auth.git"
))

description := "A small client library for Guardian Identity authentication with the Play framework"

licenses := Seq("Apache V2" -> url("http://www.apache.org/licenses/LICENSE-2.0.html"))

updateOptions := updateOptions.value.withCachedResolution(true)

resolvers += Resolver.sonatypeRepo("releases")

publishTo := Some(
  if (isSnapshot.value)
    Opts.resolver.sonatypeSnapshots
  else
    Opts.resolver.sonatypeStaging
)

libraryDependencies ++= Seq(
  play,
  identityCookie,
  identityTestUsers,
  scalaTestPlus % Test,
  scalactic % Test
)

lazy val root = project in file(".")

releaseCrossBuild := true
releaseProcess := Seq[ReleaseStep](
  checkSnapshotDependencies,
  inquireVersions,
  runClean,
  runTest,
  setReleaseVersion,
  commitReleaseVersion,
  tagRelease,
  ReleaseStep(action = releaseStepCommand("publishSigned"), enableCrossBuild = true),
  setNextVersion,
  commitNextVersion,
  ReleaseStep(action = releaseStepCommand("sonatypeReleaseAll"), enableCrossBuild = true),
  pushChanges
)
