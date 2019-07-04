package com.gu.identity.play

import cats.effect.IO
import cats.syntax.either._
import com.gu.identity.auth.IdentityAuthService
import com.gu.identity.model.User
import play.api.mvc.RequestHeader

class IdentityPlayAuthService(identityAuthService: IdentityAuthService) {

  import IdentityPlayAuthService._

  private def getCredentialsFromRequestHeader(request: RequestHeader): IO[Credentials] =
    IO.fromEither(
      Either.fromOption(
        Cookie.fromRequestHeader(request).orElse(Token.fromRequestHeader(request)),
        ifNone = new RuntimeException("neither SC_GU_U cookie or crypto access token set in request header")
      )
    )

  private def authenticateFromCredentials(credentials: Credentials): IO[String] =
    credentials match {
      case Cookie(value) => identityAuthService.authenticateSCGUUCookie(value)
      case Token(value) => identityAuthService.authenticateCryptoAccessToken(value)
    }

  def authenticateRequest(request: RequestHeader): IO[String] =
    for {
      credentials <- getCredentialsFromRequestHeader(request)
      identityId <- authenticateFromCredentials(credentials)
    } yield identityId


  private def getUserFromCredentials(credentials: Credentials): IO[User] =
    credentials match {
      case Cookie(value) => identityAuthService.getUserFromSCGUUCookie(value)
      case Token(value) => identityAuthService.getUserFromCryptoAccessToken(value)
    }

  def getUserFromRequest(request: RequestHeader): IO[User] =
    for {
      credentials <- getCredentialsFromRequestHeader(request)
      user <- getUserFromCredentials(credentials)
    } yield user
}

object IdentityPlayAuthService {

  sealed trait Credentials

  case class Cookie(value: String) extends Credentials
  object Cookie {
    def fromRequestHeader(request: RequestHeader): Option[Credentials] =
      request.headers.get("SC_GU_U").map(Cookie.apply) // TODO: correct header
  }

  case class Token(value: String) extends Credentials
  object Token {
    def fromRequestHeader(request: RequestHeader): Option[Credentials] =
      request.headers.get("Token").map(Token.apply) // TODO: correct header
  }
}