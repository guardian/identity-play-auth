package com.gu.identity.play
import java.time.Clock.systemUTC
import java.time._

import com.gu.identity.model.cookies.CookieDescriptionList
import com.gu.identity.play.idapi.CookieDescriptionJson._
import play.api.libs.json._
import play.api.mvc.Cookie

object CookieBuilder {

  def cookiesFromDescription(
    cookieDescriptionList: CookieDescriptionList,
    domain: Option[String] = None
  )(implicit clock: Clock = systemUTC()): Seq[Cookie] = {

    val maxAge = Duration.between(clock.instant(), cookieDescriptionList.expiresAt).getSeconds.toInt

    for (cookieDescription <- cookieDescriptionList.values) yield {
      val isSecure = cookieDescription.key.startsWith("SC_")
      val maxAgeOpt = if (cookieDescription.sessionCookie.contains(true)) None else Some(maxAge)
      Cookie(
        cookieDescription.key,
        cookieDescription.value,
        maxAge = maxAgeOpt,
        secure = true, // as of https://github.com/guardian/identity-frontend/pull/196
        httpOnly = isSecure, // ideallycd . this would come from the Cookie Description
        domain = domain)
    }
  }

  def fromGuestConversion(json: JsValue, domain: Option[String] = None): JsResult[Seq[Cookie]] = {
    (json \ "cookies").validate[CookieDescriptionList].map(cookiesFromDescription(_, domain))
  }
}
