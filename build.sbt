name := "identity-auth"

lazy val baseSettings = Seq(
  scalaVersion := "2.12.6",
  organization := "com.gu.identity",
  scmInfo := Some(
    ScmInfo(
      url("https://github.com/guardian/identity-auth"),
      "scm:git:git@github.com:guardian/identity-auth.git"
    )
  )
)

lazy val `identity-auth-core` = project
  .settings(baseSettings)

lazy val `identity-play-auth` = project
  .settings(baseSettings)
  .dependsOn(`identity-auth-core`)
