import ReleaseTransformations._

name := "identity-play-auth"

organization := "com.gu.identity"

scalaVersion := "2.12.6"

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
  "com.typesafe.play" %% "play" % "2.6.7",
  "com.gu.identity" %% "identity-cookie" % "3.162",
  "com.gu" %% "identity-test-users" % "0.6",
  "org.scalatestplus.play" %% "scalatestplus-play" % "3.1.2" % Test,
  "org.scalactic" %% "scalactic" % "3.0.4" % Test
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
  ReleaseStep(action = releaseStepCommand("publishSigned"), enableCrossBuild = false),
  setNextVersion,
  commitNextVersion,
  ReleaseStep(action = releaseStepCommand("sonatypeReleaseAll"), enableCrossBuild = false),
  pushChanges
)
