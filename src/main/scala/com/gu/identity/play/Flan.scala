package com.gu.identity.play

import com.gu.identity.model.cookies.CookieDescriptionList

case class UserRegistrationAndAuthenticationResult(
  user: IdUser,
  // accessToken: Option[AccessTokenDescription],
  cookies: Option[CookieDescriptionList]
)