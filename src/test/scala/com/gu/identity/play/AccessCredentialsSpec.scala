package com.gu.identity.play

import java.time.ZoneOffset.UTC
import java.time.{Clock, Instant}

import com.gu.identity.cookie.{ProductionKeys, SCUCookieData}
import com.gu.identity.play.AccessCredentials.Cookies
import com.gu.identity.play.AccessCredentials.Cookies.parseScGuU
import org.scalatestplus.play._
import play.api.test.FakeRequest

import scala.util.Try

class AccessCredentialsSpec extends PlaySpec {

  "parsing Identity cookies" must {
    def requestWith(accessCredentials: AccessCredentials.Cookies) = FakeRequest().withCookies(accessCredentials.cookies :_*)

    def authProviderWithClockSetTo(instant: Instant) =
      AccessCredentials.Cookies.authProvider(new ProductionKeys)(Clock.fixed(instant, UTC))

    val validCookies = AccessCredentials.Cookies(
      guU = "WyIxNTc0NzA5NCIsIiIsImlwYXRlc3QiLCIyIiwxNDU5ODU3MTA2MTc4LDAsMTQ1MjA4MTEwNTAwMCxmYWxzZV0.MCwCFEAUh7K0AuXPCZUjZX7I1KPApDazAhQ38PzDzsHaXQWTSpX51dylUhJcmw",
      scGuU = "WyIxNTc0NzA5NCIsMTQ1OTg1NzEwNjE3OF0.MC0CFFrjLlGPWJjIFWZskQljWSToGLkSAhUAtaF7F2IAbh4YBeIENLQOlm4FUB8"
    )

    val cookiesExpirationInstant = Instant.parse("2016-04-05T11:51:46.178Z")

    val instantBeforeExpiration = cookiesExpirationInstant.minusSeconds(1L)

    "extract an authenticated user from an HTTP request" in {
      val authProvider = authProviderWithClockSetTo(instantBeforeExpiration)

      val authenticatedIdUserOpt = authProvider(requestWith(validCookies))

      val authenticatedIdUser = authenticatedIdUserOpt.value
      authenticatedIdUser.user.id mustBe "15747094"
      authenticatedIdUser.user.displayName mustBe Some("ipatest")
      authenticatedIdUser.credentials mustBe validCookies
    }

    "report an HTTP request as unauthenticated when the current time is beyond the expiration date" in {
      val authProvider = authProviderWithClockSetTo(cookiesExpirationInstant.plusSeconds(1L))

      val authenticatedIdUserOpt = authProvider(requestWith(validCookies))

      authenticatedIdUserOpt mustBe None
    }

    "report an HTTP request as unauthenticated when signature is bad" in {
      val authProvider = authProviderWithClockSetTo(instantBeforeExpiration)

      val cookiesWithInvalidSignature = validCookies.copy(scGuU = validCookies.scGuU.dropRight(1))

      val authenticatedIdUserOpt = authProvider(requestWith(cookiesWithInvalidSignature))

      authenticatedIdUserOpt mustBe None
    }

    "report an HTTP request as unauthenticated when cookie is validly signed but has invalid content (from somewhere else!)" in {
      val authProvider = authProviderWithClockSetTo(instantBeforeExpiration)

      val authenticatedIdUserOpt = authProvider(requestWith(validCookies.copy(scGuU = validCookies.guU)))

      authenticatedIdUserOpt mustBe None
    }

    "parse Identity details out of SC_GU_U cookie json" in {
      parseScGuU("""["15747094",1459857106178]""") mustBe Some(SCUCookieData("15747094",1459857106178L))
    }
  }
}
