package com.gu.identity.play


import java.time.ZoneOffset.UTC
import java.time._
import java.time.format.DateTimeFormatter.ISO_OFFSET_DATE_TIME

import org.scalactic.Tolerance._
import org.scalatest.FreeSpec
import play.api.libs.json.{JsSuccess, Json}

class CookieBuilderTest extends FreeSpec {
  "After registering a guest user" - {
    "The response we get can be read as identity cookies" in {
      val inThreeMonths = ZonedDateTime.now(UTC).plus(Duration.ofDays(90L))

      // 2015-12-28T15:22:01+00:00
      val inThreeMonthsStr = ISO_OFFSET_DATE_TIME.format(inThreeMonths)

      val json =
        Json.parse(s"""
                      |{
                      |  "status": "ok",
                      |  "cookies": {
                      |    "values": [
                      |      {
                      |        "key": "GU_U",
                      |        "value": "gu_u_value"
                      |      },
                      |      {
                      |        "key": "SC_GU_LA",
                      |        "value": "sc_gu_la_value",
                      |        "sessionCookie": true
                      |      },
                      |      {
                      |        "key": "SC_GU_U",
                      |        "value": "sc_gu_u_value"
                      |      }
                      |    ],
                      |    "expiresAt": "$inThreeMonthsStr"
                      |  }
                      |}
        """.stripMargin)

      val domain = Some("domain")
      val idCookies = CookieBuilder.fromGuestConversion(json, domain)
      val JsSuccess(Seq(guuCookie, scgulaCookie, scguuCookie), _) = idCookies

      assert(guuCookie.name === "GU_U")
      assert(guuCookie.value === "gu_u_value")
      assert(guuCookie.maxAge.get === (90 * 24 * 60 * 60) +- 5)
      assert(guuCookie.secure) // ever since theguardian.com went full HTTPS
      assert(!guuCookie.httpOnly)
      assert(guuCookie.domain === domain)

      assert(scgulaCookie.name === "SC_GU_LA")
      assert(scgulaCookie.value === "sc_gu_la_value")
      assert(scgulaCookie.maxAge === None)
      assert(scgulaCookie.secure)
      assert(scgulaCookie.httpOnly)
      assert(scgulaCookie.domain === domain)

      assert(scguuCookie.name === "SC_GU_U")
      assert(scguuCookie.value === "sc_gu_u_value")
      assert(scguuCookie.maxAge.get === (90 * 24 * 60 * 60) +- 5)
      assert(scguuCookie.secure)
      assert(scguuCookie.httpOnly)
      assert(scguuCookie.domain === domain)
    }
  }
}
