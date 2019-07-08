package com.gu.identity.auth

import cats.effect.IO
import com.gu.identity.model.User
import org.http4s.Uri

import scala.concurrent.ExecutionContext

class IdentityAuthService private (identityClient: IdentityClient) {

  def authenticateUser(credentials: UserCredentials): IO[String] =
    identityClient.authenticateUser(credentials).map(_.userId)

  def getUserFromCredentials(credentials: UserCredentials): IO[User] =
    identityClient.getUserFromCredentials(credentials).map(_.user)
}

object IdentityAuthService {

  def apply(identityApiUri: Uri, accessToken: String)(implicit ec: ExecutionContext): IdentityAuthService = {
    val identityClient = new IdentityClient(identityApiUri, accessToken)
    new IdentityAuthService(identityClient)
  }
}
