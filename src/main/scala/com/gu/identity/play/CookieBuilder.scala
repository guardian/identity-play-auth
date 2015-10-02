package com.gu.identity.play
import org.joda.time.{DateTime, Duration}
import play.api.libs.functional.syntax._
import play.api.libs.json.Reads._
import play.api.libs.json._
import play.api.mvc.Cookie

import scala.util.Try

object CookieBuilder {
  def fromGuestConversion(json: JsValue, domain: Option[String] = None): Option[Seq[Cookie]] = {
    def cookieRead(maxAge: Int): Reads[Cookie] = (
      (JsPath \ "key").read[String] and
        (JsPath \ "value").read[String] and
        (JsPath \ "sessionCookie").readNullable[Boolean]
      ) { (name, value, sessionCookie) =>
        val secure = name.startsWith("SC_")
        val maxAgeOpt = if (sessionCookie.contains(true)) None else Some(maxAge)
        Cookie(name, value, maxAge = maxAgeOpt, secure = secure, httpOnly = secure, domain = domain)
      }

    for {
      expirationString <- (json \ "cookies" \ "expiresAt").asOpt[String]
      expiration <- Try { new DateTime(expirationString) }.toOption
      maxAge = new Duration(DateTime.now, expiration).getStandardSeconds.toInt
      cookies <- (json \ "cookies" \ "values").asOpt(Reads.seq[Cookie](cookieRead(maxAge)))
    } yield cookies
  }
}
