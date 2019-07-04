package com.gu.identity.auth

import cats.effect.IO
import com.gu.identity.model.User

import scala.concurrent.ExecutionContext

class IdentityAuthService private (identityClient: IdentityClient) {

  def authenticateSCGUUCookie(cookie: String): IO[String] =
    identityClient.authenticateSCGUUCookie(cookie).map(_.userId)

  def authenticateCryptoAccessToken(token: String): IO[String] =
    identityClient.authenticateCryptoAccessToken(token).map(_.userId)

  def getUserFromSCGUUCookie(cookie: String): IO[User] =
    identityClient.getUserFromSCGUUCookie(cookie).map(_.user)

  def getUserFromCryptoAccessToken(token: String): IO[User] =
    identityClient.getUserFromCryptoAccessToken(token).map(_.user)
}

object IdentityAuthService {

  // TODO: validate domain (?)
  def apply(domain: String, accessToken: String)(implicit ec: ExecutionContext): IdentityAuthService = {
    val identityClient = new IdentityClient(domain, accessToken)
    new IdentityAuthService(identityClient)
  }
}
