package com.gu.identity.play

import com.gu.identity.cookie.IdentityKeys
import play.api.mvc.{Request, RequestHeader}

// TODO: remove - not adding any useful functionality
trait AuthenticationService {

  val identityKeys: IdentityKeys

  lazy val authenticatedIdUserProvider: AuthenticatedIdUser.Provider =
    AccessCredentials.Cookies.authProvider(identityKeys)

  def authenticatedUserFor[A](request: RequestHeader) = authenticatedIdUserProvider(request)

  def requestPresentsAuthenticationCredentials(request: Request[_]) = authenticatedUserFor(request).isDefined

}
