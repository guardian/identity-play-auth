package com.gu.identity.auth

import cats.effect._
import io.circe.Decoder
import io.circe.generic.semiauto.deriveDecoder
import org.http4s.{Header, Headers, Request, Response, Status, Uri}
import org.http4s.circe._
import org.http4s.client.blaze.BlazeClientBuilder
import com.gu.identity.model.User

import scala.concurrent.ExecutionContext

private class IdentityClient(domain: String, serverAccessToken: String)(implicit ec: ExecutionContext)
  extends CirceEntityDecoder {

  import IdentityClient._

  private implicit val cs: ContextShift[IO] = IO.contextShift(ec)

  private def executeGetRequest[A : Decoder](headers: Headers, path: String): IO[A] =
    BlazeClientBuilder[IO](ec).resource.use { client =>
      for {
        uri <- IO.fromEither(Uri.fromString(s"https://$domain$path"))
        request = Request[IO](uri = uri, headers = headers)
        response <- client.fetch(request)(decodeResponse[A])
      } yield response
    }

  // TODO: correct headers
  private def headersForSCGUUCookie(cookie: String): Headers =
    Headers.of(
      Header("New-Server-Access-Token", serverAccessToken),
      Header("SC-GU-U", cookie)
    )

  // TODO: correct headers
  private def headersForCryptoAccessToken(token: String): Headers =
    Headers.of(
      Header("New-Server-Access-Token", serverAccessToken),
      Header("Crypto-Access-Token", token)
    )

  def authenticateSCGUUCookie(cookie: String): IO[AuthResponse] =
    executeGetRequest[AuthResponse](headersForSCGUUCookie(cookie), path = "/auth/id")

  def authenticateCryptoAccessToken(token: String): IO[AuthResponse] =
    executeGetRequest[AuthResponse](headersForCryptoAccessToken(token), path = "/auth/id")

  def getUserFromSCGUUCookie(cookie: String): IO[UserResponse] =
    executeGetRequest[UserResponse](headersForSCGUUCookie(cookie), path = "/user/me")

  def getUserFromCryptoAccessToken(token: String): IO[UserResponse] =
    executeGetRequest[UserResponse](headersForCryptoAccessToken(token), path = "/user/me")
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