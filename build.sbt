import sbtrelease.ReleaseStateTransformations._

name := "identity-auth"

lazy val baseSettings = Seq(
  scalaVersion := "2.12.6",
  organization := "com.gu.identity",
  resolvers += Resolver.sonatypeRepo("releases")
)

lazy val publishSettings = Seq(
  releaseCrossBuild := true,
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
  ),
  licenses := Seq("Apache V2" -> url("http://www.apache.org/licenses/LICENSE-2.0.html")),
  publishTo := Some(
    if (isSnapshot.value)
      Opts.resolver.sonatypeSnapshots
    else
      Opts.resolver.sonatypeStaging
  ),
  scmInfo := Some(
    ScmInfo(
      url("https://github.com/guardian/identity-auth"),
      "scm:git:git@github.com:guardian/identity-auth.git"
    )
  )
)

lazy val allSettings = baseSettings ++ publishSettings

lazy val `identity-auth-core` = project
  .settings(
    allSettings,
    name := "identity-auth-core",
    libraryDependencies ++= Seq(
      "com.gu.identity" %% "identity-model" % "3.181",
      "io.circe" %% "circe-core" % "0.10.0",
      "io.circe" %% "circe-generic" % "0.10.0",
      "io.circe" %% "circe-parser" % "0.10.0",
      "org.http4s" %% "http4s-dsl" % "0.20.3",
      "org.http4s" %% "http4s-blaze-client" % "0.20.3",
      "org.http4s" %% "http4s-blaze-server" % "0.20.3",
      "org.http4s" %% "http4s-circe" % "0.20.3"
    )
  )

lazy val `identity-play-auth` = project
  .settings(
    allSettings,
    name := "identity-play-auth",
    libraryDependencies ++= Seq(
      "com.typesafe.play" %% "play" % "2.6.7",
      "com.gu.identity" %% "identity-cookie" % "3.162",
      "com.gu" %% "identity-test-users" % "0.6",
      "org.scalatestplus.play" %% "scalatestplus-play" % "3.1.2" % Test,
      "org.scalactic" %% "scalactic" % "3.0.4" % Test
    )
  )
  .dependsOn(`identity-auth-core`)
