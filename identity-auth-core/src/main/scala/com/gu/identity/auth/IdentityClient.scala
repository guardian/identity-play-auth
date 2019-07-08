package com.gu.identity.auth

import cats.effect._
import io.circe.Decoder
import io.circe.generic.semiauto.deriveDecoder
import org.http4s.{Header, Headers, Request, Response, Status, Uri}
import org.http4s.circe._
import org.http4s.client.blaze.BlazeClientBuilder
import com.gu.identity.model.User

import scala.concurrent.ExecutionContext

private class IdentityClient(identityApiUri: Uri, serverAccessToken: String)(implicit ec: ExecutionContext)
  extends CirceEntityDecoder {

  import IdentityClient._

  private implicit val cs: ContextShift[IO] = IO.contextShift(ec)

  private def executeGetRequest[A : Decoder](headers: Headers, path: String): IO[A] =
    BlazeClientBuilder[IO](ec).resource.use { client =>
      val request = Request[IO](uri = identityApiUri / path, headers = headers)
      client.fetch(request)(decodeResponse[A])
    }

  private def headersForCredentials(credentials: UserCredentials): Headers =
    credentials match {
      case UserCredentials.SCGUUCookie(value) =>
        Headers.of(
          Header("New-Server-Access-Token", serverAccessToken),
          Header("SC-GU-U", value)
        )
      case UserCredentials.CryptoAccessToken(value) =>
        Headers.of(
          Header("New-Server-Access-Token", serverAccessToken),
          Header("Crypto-Access-Token", value)
        )
    }

  def authenticateUser(credentials: UserCredentials): IO[AuthResponse] =
    executeGetRequest[AuthResponse](headersForCredentials(credentials), path = "auth/id")

  def getUserFromCredentials(credentials: UserCredentials): IO[UserResponse] =
    executeGetRequest[UserResponse](headersForCredentials(credentials), path = "user/me")
}


private object IdentityClient extends CirceEntityDecoder with UserDecoder {

  case class AuthResponse(userId: String)
  object AuthResponse {
    implicit val authResponseDecoder: Decoder[AuthResponse] = deriveDecoder[AuthResponse]
  }

  case class UserResponse(user: User)
  object UserResponse {
    implicit val userResponseDecoder: Decoder[UserResponse] = deriveDecoder[UserResponse]
  }

  case class Error(message: String)
  object Error {
    implicit val errorDecoder: Decoder[Error] = deriveDecoder[Error]
  }

  case class Errors(errors: List[Error]) extends Exception
  object Errors {
    implicit val errorsDecoder: Decoder[Errors] = deriveDecoder[Errors]
  }

  def decodeResponse[A : Decoder](response: Response[IO]): IO[A] =
    response match {
      case Status.Successful(_) => response.as[A]
      case _ => response.as[Errors].flatMap(IO.raiseError[A])
    }
}