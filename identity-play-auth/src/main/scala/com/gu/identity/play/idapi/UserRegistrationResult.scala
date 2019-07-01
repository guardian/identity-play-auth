package com.gu.identity.play.idapi

import com.gu.identity.model.cookies.{CookieDescription, CookieDescriptionList}
import com.gu.identity.play.IdUser
import play.api.libs.json.Json

object CookieDescriptionJson {

  implicit val readsCookieDescriptionList = Json.reads[CookieDescription]

  implicit val readsCookieList = Json.reads[CookieDescriptionList]
}

case class UserRegistrationResult(
  user: IdUser,
  // accessToken: Option[AccessTokenDescription],
  cookies: Option[CookieDescriptionList] // `authenticate=true&format=cookies` - see https://github.com/guardian/identity/pull/621
)

object UserRegistrationResult {
  import CookieDescriptionJson._

  implicit val readsResult = Json.reads[UserRegistrationResult]
}
