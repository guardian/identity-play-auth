package com.gu.identity.play

import java.time.Clock
import java.time.Clock.systemUTC

import com.github.nscala_time.time.Imports._
import com.gu.identity.cookie.{IdentityCookieDecoder, IdentityKeys, SCUCookieData}
import com.gu.identity.model.{User, CryptoAccessToken, LiftJsonConfig}
import com.gu.identity.play.AccessCredentials.Cookies.SC_GU_U
import com.gu.identity.play.AuthenticatedIdUser.Provider
import com.gu.identity.signing.{CollectionSigner, DsaService, StringSigner}
import org.joda.time.DateTime
import play.api.Logger
import play.api.libs.json._
import play.api.mvc.{Cookie, RequestHeader}

import scala.util.Try

case class AuthenticatedIdUser(credentials: AccessCredentials, user: IdMinimalUser)

object AuthenticatedIdUser {
  implicit def authenticatedIdUserToMinimalUser(aid: AuthenticatedIdUser) = aid.user

  type Provider = RequestHeader => Option[AuthenticatedIdUser]

  def provider(providers: Provider*) = {
    r : RequestHeader =>
      val users = providers.flatMap(_ (r)) // may be differing users!

      for {
        principal <- users.headOption if users.forall(_.id == principal.id) // reject ambiguity of different users!
      } yield {
        val displayNameOpt = users.flatMap(_.displayName).headOption
        principal.copy(user = principal.user.copy(displayName = displayNameOpt))
      }
  }
}

sealed trait AccessCredentials

object AccessCredentials {
  case class Cookies(scGuU: String) extends AccessCredentials {
    val forwardingHeader = "X-GU-ID-FOWARDED-SC-GU-U" -> scGuU

    val cookies = Seq(
      Cookie(SC_GU_U, scGuU)
    )
  }

  object Cookies {
    val GU_U = "GU_U"
    val SC_GU_U = "SC_GU_U"

    val logger = Logger(getClass)

    def authProvider(identityKeys: IdentityKeys)(implicit clock: Clock = systemUTC): Provider = {
      val cookieDecoder = new IdentityCookieDecoder(identityKeys)
      val signer = new StringSigner(new DsaService(Some(identityKeys.publicDsaKey), None))

      def displayNameFrom(request: RequestHeader, id: String): Option[String] = for {
        guU <- request.cookies.get("GU_U")
        guUCookieData <- cookieDecoder.getUserDataForGuU(guU.value)
        user = guUCookieData.user if user.id == id
        displayName <- user.publicFields.displayName
      } yield displayName

      def secureDataFrom(scGuU: Cookie): Option[SCUCookieData] = for {
        correctlySignedString <- Try(signer.getStringForSignedString(scGuU.value)).getOrElse { logger.warn(s"Bad sig on $scGuU"); None }
        secureCookieData <- parseScGuU(correctlySignedString) if secureCookieData.expiry > clock.millis()
      } yield secureCookieData

      request => for {
        scGuU <- request.cookies.get(SC_GU_U)
        secureCookieData <- secureDataFrom(scGuU)
      } yield AuthenticatedIdUser(
        AccessCredentials.Cookies(scGuU.value),
        IdMinimalUser(secureCookieData.id, displayNameFrom(request, secureCookieData.id))
      )
    }

    def parseScGuU(correctlySignedString: String): Option[SCUCookieData] = Json.parse(correctlySignedString) match {
      case JsArray(elements) => elements.toList match {
        case JsString(id) :: JsNumber(expiry) :: Nil => Some(SCUCookieData(id, expiry.toLongExact))
        case _ => None
      }
      case _ => None
    }
  }

  case class Token(tokenText: String) extends AccessCredentials

  object Token {
    val Header = "GU-IdentityToken"

    /** @param targetClientId Not confidential, eg "membership" https://github.com/guardian/identity-token-auth-sample/blob/e640832d/main.scala#L28
      */
    def authProvider(identityKeys: IdentityKeys, targetClientId: String): Provider = {
      val collectionSigner = new CollectionSigner(new StringSigner(new DsaService(identityKeys.publicDsaKey, null)), LiftJsonConfig.formats)

      // Adapted from https://github.com/guardian/identity/blob/8663b03/identity-api-client-lib/src/main/java/com/gu/identity/client/IdentityApiClient.java#L321-L334
      def extractUserDataFromToken(tokenString: String): Either[String, User] = {
        val cryptoAccessToken = collectionSigner.getValueForSignedString[CryptoAccessToken](tokenString)

        cryptoAccessToken map { cryptoToken =>
          if (cryptoToken.expiryTime < DateTime.now) {
            Left(s"Token: $tokenString has expired")
          } else if (cryptoToken.targetClient != targetClientId) {
            Left(s"Token: $tokenString was not targeted for the client '$targetClientId'")
          } else Right(cryptoToken.getUser)
        } getOrElse Left(s"Token: Missing or invalid token")
      }

      request => for {
        tokenText <- request.headers.get(Token.Header)
        user <- extractUserDataFromToken(tokenText).right.toOption
      } yield AuthenticatedIdUser(
        AccessCredentials.Token(tokenText),
        IdMinimalUser.from(user)
      )
    }
  }
}

