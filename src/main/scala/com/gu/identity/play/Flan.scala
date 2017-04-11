package com.gu.identity.play

import com.gu.identity.model.cookies.{CookieDescription, CookieDescriptionList}
import play.api.libs.json.Json

case class UserRegistrationAndAuthenticationResult(
  user: IdUser,
  // accessToken: Option[AccessTokenDescription],
  cookies: Option[CookieDescriptionList]
)

object CookieDescriptionJson {

  implicit val readsCookieDescriptionList = Json.reads[CookieDescription]

  implicit val readsCookieList = Json.reads[CookieDescriptionList]
}

object UserRegistrationAndAuthenticationResult {
  import CookieDescriptionJson._

  implicit val readsResult = Json.reads[UserRegistrationAndAuthenticationResult]
}
