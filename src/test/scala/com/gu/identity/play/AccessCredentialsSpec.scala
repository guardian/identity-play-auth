package com.gu.identity.play

import java.time.ZoneOffset.UTC
import java.time.{Clock, Instant}

import com.gu.identity.cookie.{ProductionKeys, SCUCookieData}
import com.gu.identity.play.AccessCredentials.Cookies._
import com.gu.identity.play.AccessCredentials.{Cookies, Token}
import org.joda.time.DateTimeUtils
import org.scalatestplus.play._
import play.api.mvc.Cookie
import play.api.test.FakeRequest

class AccessCredentialsSpec extends PlaySpec {

  case class ExampleUser(
                          cookieCreds: Cookies,
                          tokenCreds: Token,
                          user: IdMinimalUser
  ) {
      val justScGuUCookie = cookieCreds.cookies.filter(_.name == SC_GU_U)
      val justGuUCookie = cookieCreds.cookies.filter(_.name == GU_U)

      val cookieCredsWithJustScGuU = cookieCreds.copy(guU = None)
  }

  val Fred = ExampleUser(
    AccessCredentials.Cookies(
      scGuU = "WyIxNTk4MDY5OCIsMTQ2NTMxMjU0Njk5M10.MCwCFADn0bo9Vn8UBzvqp-HS8Tqpg0ZOAhRUIGLF9CMQLclx3Ah5rVz_Ivzc9w",
      guU = Some("WyIxNTk4MDY5OCIsIiIsIkZyZWRBdXRoVGVzdCIsIjIiLDE0NjUzMTI1NDY5OTMsMSwxNDU3NTM2MzQxMDAwLGZhbHNlXQ.MC0CFBDmPMIyz4FZjc8DOTSujdDWrQ1uAhUAv32hMXHW-98dcZrHGkf0ZsF5UYU")
    ),
    AccessCredentials.Token("eyJ1c2VyIjp7InByaW1hcnlFbWFpbEFkZHJlc3MiOm51bGwsImlkIjoiMTU5ODA2OTgiLCJwdWJsaWNGaWVsZHMiOnsiZGlzcGxheU5hbWUiOiJGcmVkQXV0aFRlc3QifSwicHJpdmF0ZUZpZWxkcyI6e30sInN0YXR1c0ZpZWxkcyI6eyJ1c2VyRW1haWxWYWxpZGF0ZWQiOmZhbHNlfSwiZGF0ZXMiOnsiYWNjb3VudENyZWF0ZWREYXRlIjoiMjAxNi0wMy0wOVQxNToxMjoyMVoifSwidXNlckdyb3VwcyI6W10sInNvY2lhbExpbmtzIjpbXSwiYWREYXRhIjp7fX0sImV4cGlyeVRpbWUiOiIyMDE4LTAzLTA5VDE2OjIzOjM3WiIsImlzc3VlZFRpbWUiOiIyMDE2LTAzLTA5VDE2OjIzOjM3WiIsInRhcmdldENsaWVudCI6Im1lbWJlcnNoaXAiLCJpc3N1ZWRDbGllbnQiOiJndS1hbmRyb2lkLW5ld3MifQ.MCwCFCmeJ4FFnCeEchhyOpgf4DVmu4D8AhRcSe2rq7CTeietBB8VXu03n5GRzw"),
    IdMinimalUser(id = "15980698", displayName= Some("FredAuthTest"))
  )

  val Bob = ExampleUser(
    AccessCredentials.Cookies(
      scGuU = "WyIxNTk4MDczNyIsMTQ2NTMxMjk1ODEzNV0.MCwCFBXRJy3y6wG5C-L2nX13fbtw4_6lAhREra7GO2hdGC7NPaVeew9IRuU-gA",
      guU = Some("WyIxNTk4MDczNyIsIiIsIkJvYkF1dGhUZXN0IiwiMiIsMTQ2NTMxMjk1ODEzNSwxLDE0NTc1MzY5NTcwMDAsZmFsc2Vd.MCwCFENuiIJHWmN7x5dyZFMeGA2FvqADAhQJ8rrFP61ijW3UsRlbT6JjsL4ZPQ")
    ),
    AccessCredentials.Token("eyJ1c2VyIjp7InByaW1hcnlFbWFpbEFkZHJlc3MiOm51bGwsImlkIjoiMTU5ODA3MzciLCJwdWJsaWNGaWVsZHMiOnsiZGlzcGxheU5hbWUiOiJCb2JBdXRoVGVzdCJ9LCJwcml2YXRlRmllbGRzIjp7fSwic3RhdHVzRmllbGRzIjp7InVzZXJFbWFpbFZhbGlkYXRlZCI6ZmFsc2V9LCJkYXRlcyI6eyJhY2NvdW50Q3JlYXRlZERhdGUiOiIyMDE2LTAzLTA5VDE1OjIyOjM3WiJ9LCJ1c2VyR3JvdXBzIjpbXSwic29jaWFsTGlua3MiOltdLCJhZERhdGEiOnt9fSwiZXhwaXJ5VGltZSI6IjIwMTgtMDMtMDlUMTY6MjU6MTVaIiwiaXNzdWVkVGltZSI6IjIwMTYtMDMtMDlUMTY6MjU6MTVaIiwidGFyZ2V0Q2xpZW50IjoibWVtYmVyc2hpcCIsImlzc3VlZENsaWVudCI6Imd1LWFuZHJvaWQtbmV3cyJ9.MC0CFQCpO3t-nAnUapUTzBaQdi2aZ9bCawIUYq7CanuVJjnDjKtIiL3zIsONk1k"),
    IdMinimalUser(id = "15980737", displayName= Some("BobAuthTest"))
  )


  val BobCookiesExpirationInstant = Instant.parse("2016-06-07T15:22:38.135Z")

  val timeBeforeExpiration = BobCookiesExpirationInstant.minusSeconds(600L)

  def requestWith(cookies: Seq[Cookie]) = FakeRequest().withCookies(cookies :_*)

  "parsing Identity cookies" must {

    def authProviderWithClockSetTo(instant: Instant) = {
      DateTimeUtils.setCurrentMillisFixed(instant.toEpochMilli) // necessary to handle https://github.com/guardian/identity/blob/f41ad5cc/identity-cookie/src/main/scala/com/gu/identity/cookie/GuUDecoder.scala#L25
      AccessCredentials.Cookies.authProvider(new ProductionKeys)(Clock.fixed(instant, UTC))
    }

    "extract an authenticated user from an HTTP request" in {
      val authProvider = authProviderWithClockSetTo(timeBeforeExpiration)

      val authenticatedIdUser = authProvider(requestWith(Bob.justScGuUCookie)).value

      authenticatedIdUser.id mustBe Bob.user.id
      authenticatedIdUser.credentials mustBe Bob.cookieCredsWithJustScGuU
    }

    "extract an authenticated user from an HTTP request, with the display name if the GU_U cookie is available" in {
      val authProvider = authProviderWithClockSetTo(timeBeforeExpiration)

      val authenticatedIdUserOpt = authProvider(requestWith(Bob.cookieCreds.cookies))

      val authenticatedIdUser = authenticatedIdUserOpt.value

      authenticatedIdUser.credentials mustBe Bob.cookieCreds
      authenticatedIdUser.user.displayName mustBe Some("BobAuthTest")
      authenticatedIdUser.user mustBe Bob.user
    }

    "report an HTTP request as unauthenticated when the current time is beyond the expiration date" in {
      val authProvider = authProviderWithClockSetTo(BobCookiesExpirationInstant.plusSeconds(1L))

      val authenticatedIdUserOpt = authProvider(requestWith(Bob.justScGuUCookie))

      authenticatedIdUserOpt mustBe None
    }

    "report an HTTP request as unauthenticated when signature is bad" in {
      val authProvider = authProviderWithClockSetTo(timeBeforeExpiration)

      val cookiesWithInvalidSignature = Bob.cookieCreds.copy(scGuU = Bob.cookieCreds.scGuU.dropRight(1))

      val authenticatedIdUserOpt = authProvider(requestWith(cookiesWithInvalidSignature.cookies))

      authenticatedIdUserOpt mustBe None
    }

    "report an HTTP request as unauthenticated when cookie is validly signed but has invalid content (from somewhere else!)" in {
      val authProvider = authProviderWithClockSetTo(timeBeforeExpiration)

      val authenticatedIdUserOpt = authProvider(requestWith(Bob.justGuUCookie))

      authenticatedIdUserOpt mustBe None
    }

    "parse Identity details out of SC_GU_U cookie json" in {
      parseScGuU("""["15747094",1459857106178]""") mustBe Some(SCUCookieData("15747094",1459857106178L))
    }
  }

  "parsing requests that accept multiple authentication methods" must {

    val authProvider =
      AuthenticatedIdUser.provider(Cookies.authProvider(new ProductionKeys)(Clock.fixed(timeBeforeExpiration, UTC)), Token.authProvider(new ProductionKeys, "membership"))

    "authenticate the request if it has only a valid token" in {
      authProvider(FakeRequest().withHeaders(Token.Header -> Bob.tokenCreds.tokenText)).value.user.id mustBe Bob.user.id
      authProvider(FakeRequest().withHeaders(Token.Header -> Fred.tokenCreds.tokenText)).value.user.id mustBe Fred.user.id
    }

    "authenticate the request if it has only a valid cookie" in {
      authProvider(requestWith(Bob.justScGuUCookie)).value.user.id mustBe Bob.user.id
      authProvider(requestWith(Fred.justScGuUCookie)).value.user.id mustBe Fred.user.id
    }

    "authenticate the request if it contains identical users, returning the credentials from the first match" in {
      val authenticatedIdUser = authProvider(requestWith(Bob.justScGuUCookie).withHeaders(Token.Header -> Bob.tokenCreds.tokenText)).value
      authenticatedIdUser.credentials mustBe Bob.cookieCredsWithJustScGuU
      authenticatedIdUser.user.id mustBe Bob.user.id
    }

    "extract the display name from token if it's provided" in {
      authProvider(requestWith(Bob.justScGuUCookie).withHeaders(Token.Header -> Bob.tokenCreds.tokenText)).value.user.displayName mustBe Bob.user.displayName
    }

    "not authenticate the request if it contains differing users" in {
      authProvider(requestWith(Bob.justScGuUCookie).withHeaders(Token.Header -> Fred.tokenCreds.tokenText)) mustBe None
    }
  }
}
