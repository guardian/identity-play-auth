package com.gu.identity.play
import org.joda.time.{DateTime, Duration}
import play.api.libs.functional.syntax._
import play.api.libs.json.Reads._
import play.api.libs.json._
import play.api.mvc.Cookie

import scala.util.Try

object CookieBuilder {
  def fromGuestConversion(json: JsValue, domain: Option[String] = None): Option[Seq[Cookie]] = {
    implicit val cookieRead: Reads[Cookie] = (
      (JsPath \ "key").read[String] and
        (JsPath \ "value").read[String]
      )(Cookie.apply(_, _))

    for {
      expirationString <- (json \ "cookies" \ "expiresAt").asOpt[String]
      expiration <- Try { new DateTime(expirationString) }.toOption
      maxAge = new Duration(DateTime.now, expiration).getStandardSeconds.toInt
      cookies <- (json \ "cookies" \ "values").asOpt[Seq[Cookie]]
      guuCookie <- cookies.find(_.name == "GU_U")
      scguuCookie <- cookies.find(_.name == "SC_GU_U")
      scgulaCookie <- cookies.find(_.name == "SC_GU_LA")
    } yield {
      Seq(
        guuCookie.copy(maxAge = Some(maxAge), secure = false, httpOnly = false, domain = domain),
        scguuCookie.copy(maxAge = Some(maxAge), secure = true, httpOnly = true, domain = domain),
        scgulaCookie.copy(secure = true, httpOnly = true, domain = domain)
      )
    }
  }
}
