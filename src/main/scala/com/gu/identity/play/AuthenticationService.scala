package com.gu.identity.play

import com.gu.identity.cookie.{IdentityCookieDecoder, IdentityKeys}
import play.api.mvc.{Request, RequestHeader}

trait AuthenticationService {

  val identityKeys: IdentityKeys

  lazy val cookieDecoder = new IdentityCookieDecoder(identityKeys)

  def authenticatedUserFor[A](request: RequestHeader): Option[AuthenticatedIdUser] = for {
    scGuU <- request.cookies.get("SC_GU_U")
    guU <- request.cookies.get("GU_U")
    minimalSecureUser <- cookieDecoder.getUserDataForScGuU(scGuU.value)
    guUCookieData <- cookieDecoder.getUserDataForGuU(guU.value)
    user = guUCookieData.user if user.id == minimalSecureUser.id
  } yield AuthenticatedIdUser(
    scGuU.value,
    IdMinimalUser(user.id, user.publicFields.displayName)
  )

  def requestPresentsAuthenticationCredentials(request: Request[_]) = authenticatedUserFor(request).isDefined
}
