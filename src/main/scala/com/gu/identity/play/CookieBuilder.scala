package com.gu.identity.play
import play.api.libs.functional.syntax._
import play.api.libs.json.Reads._
import play.api.libs.json._
import play.api.mvc.Cookie
import java.time._

object CookieBuilder {
  def fromGuestConversion(json: JsValue, domain: Option[String] = None): JsResult[Seq[Cookie]] = {
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
      expiration <- (json \ "cookies" \ "expiresAt").validate[ZonedDateTime]
      maxAge = Duration.between(Instant.now(), expiration).getSeconds.toInt
      cookies <- (json \ "cookies" \ "values").validate(Reads.seq[Cookie](cookieRead(maxAge)))
    } yield cookies
  }
}
