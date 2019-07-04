name := "identity-auth-core"

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
