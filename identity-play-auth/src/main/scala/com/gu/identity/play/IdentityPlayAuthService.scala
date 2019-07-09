package com.gu.identity.play

import cats.effect.IO
import cats.syntax.either._
import com.gu.identity.auth.{IdentityAuthService, UserCredentials}
import com.gu.identity.auth.UserCredentials.{CryptoAccessToken, SCGUUCookie}
import com.gu.identity.model.User
import org.http4s.Uri
import play.api.mvc.RequestHeader

import scala.concurrent.ExecutionContext

class IdentityPlayAuthService(identityAuthService: IdentityAuthService) {

  import IdentityPlayAuthService._

  def authenticateRequest(request: RequestHeader): IO[(UserCredentials, String)] =
    for {
      credentials <- getUserCredentialsFromRequest(request)
      identityId <- identityAuthService.authenticateUser(credentials)
    } yield (credentials, identityId)

  def getUserFromRequest(request: RequestHeader): IO[(UserCredentials, User)] =
    for {
      credentials <- getUserCredentialsFromRequest(request)
      user <- identityAuthService.getUserFromCredentials(credentials)
    } yield (credentials, user)
}

object IdentityPlayAuthService {

  private def getUserCredentialsFromRequest(request: RequestHeader): IO[UserCredentials] = {
    val cookie = request.headers.get("SC_GU_U").map(SCGUUCookie.apply)
    val token = request.headers.get("Token").map(CryptoAccessToken.apply)
    IO.fromEither(
      Either.fromOption(
        cookie.orElse(token),
        ifNone = new RuntimeException("neither SC_GU_U cookie or crypto access token set in request header")
      )
    )
  }

  def apply(identityApiUri: Uri, accessToken: String)(implicit ec: ExecutionContext): IdentityPlayAuthService = {
    val identityAuthService = IdentityAuthService(identityApiUri, accessToken)
    new IdentityPlayAuthService(identityAuthService)
  }
}