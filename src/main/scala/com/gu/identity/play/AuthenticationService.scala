package com.gu.identity.play

import com.gu.identity.cookie.{IdentityCookieDecoder, IdentityKeys}
import play.api.mvc.{Request, RequestHeader}

trait AuthenticationService {
  def idWebAppSigninUrl(returnUrl: String) : String

  val identityKeys: IdentityKeys

  val cookieDecoder = new IdentityCookieDecoder(identityKeys)

  def authenticatedUserFor[A](request: RequestHeader): Option[IdMinimalUser] = for {
    scGuU <- request.cookies.get("SC_GU_U")
    guU <- request.cookies.get("GU_U")
    minimalSecureUser <- cookieDecoder.getUserDataForScGuU(scGuU.value)
    guUCookieData <- cookieDecoder.getUserDataForGuU(guU.value)
    user = guUCookieData.user if user.id == minimalSecureUser.id
  } yield IdMinimalUser(user.id, user.publicFields.displayName)

  def requestPresentsAuthenticationCredentials(request: Request[_]) = authenticatedUserFor(request).isDefined
}
