import ReleaseTransformations._
import Dependencies._

name := "identity-play-auth"

organization := "com.gu.identity"

scalaVersion := "2.11.6"

crossScalaVersions := Seq(scalaVersion.value)

scmInfo := Some(ScmInfo(
  url("https://github.com/guardian/identity-play-auth"),
  "scm:git:git@github.com:guardian/identity-play-auth.git"
))

description := "A small client library for Guardian Identity authentication with the Play framework"

licenses := Seq("Apache V2" -> url("http://www.apache.org/licenses/LICENSE-2.0.html"))

resolvers += "Guardian Github Releases" at "http://guardian.github.io/maven/repo-releases"

libraryDependencies ++= Seq(
  play,
  nscalaTime,
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
  ReleaseStep(action = Command.process("publishSigned", _)),
  setNextVersion,
  commitNextVersion,
  ReleaseStep(action = Command.process("sonatypeReleaseAll", _)),
  pushChanges
)
