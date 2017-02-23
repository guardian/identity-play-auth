package com.gu.identity.play

import java.time.Clock
import java.time.Clock.systemUTC

import com.github.nscala_time.time.Imports._
import com.gu.identity.cookie.{GuUCookieData, IdentityCookieDecoder, IdentityKeys, SCUCookieData}
import com.gu.identity.model.{CryptoAccessToken, LiftJsonConfig, User}
import com.gu.identity.play.AccessCredentials.Cookies._
import com.gu.identity.play.AuthenticatedIdUser.Provider
import com.gu.identity.signing.{CollectionSigner, DsaService, StringSigner}
import org.joda.time.DateTime
import play.api.Logger
import play.api.libs.json._
import play.api.mvc.{Cookie, RequestHeader}

import scala.language.implicitConversions
import scala.util.Try

case class AuthenticatedIdUser(credentials: AccessCredentials, user: IdMinimalUser) {

  def setDisplayName(displayNameOpt: Option[String]):AuthenticatedIdUser =
    copy(user = user.copy(displayName = displayNameOpt))

}

object AuthenticatedIdUser {
  implicit def authenticatedIdUserToMinimalUser(aid: AuthenticatedIdUser): IdMinimalUser = aid.user

  type Provider = RequestHeader => Option[AuthenticatedIdUser]

  def provider(providers: Provider*) = {
    r : RequestHeader =>
      val users = providers.flatMap(_ (r)) // may be differing users!

      for {
        principal <- users.headOption if users.forall(_.id == principal.id) // reject ambiguity of different users!
      } yield principal.setDisplayName(users.flatMap(_.displayName).headOption)
  }

  implicit class RichProvider(provider: Provider) {
    /**
      * @return the authenticated user, with the display name from the additionalDisplayNameProvider, so long
      *         as the Identity Id matches
      */
    def withDisplayNameProvider(additionalDisplayNameProvider: Provider): Provider = { req: RequestHeader =>
      provider(req).map { authenticatedUser =>
        if (authenticatedUser.user.displayName.isDefined) authenticatedUser else (for {
          u <- additionalDisplayNameProvider(req) if u.id == authenticatedUser.id
        } yield authenticatedUser.setDisplayName(u.user.displayName)).getOrElse(authenticatedUser)
      }
    }
  }
}

sealed trait AccessCredentials

object AccessCredentials {
  case class Cookies(scGuU: String, guU: Option[String] = None) extends AccessCredentials {
    val forwardingHeader = "X-GU-ID-FOWARDED-SC-GU-U" -> scGuU

    val cookies = Seq(
      Cookie(SC_GU_U, scGuU)
    ) ++ guU.map(c => Cookie(GU_U, c))
  }

  object Cookies {
    val GU_U = "GU_U"
    val SC_GU_U = "SC_GU_U"

    val logger = Logger(getClass)

    def authProvider(identityKeys: IdentityKeys)(implicit clock: Clock = systemUTC): Provider = {
      val cookieDecoder = new IdentityCookieDecoder(identityKeys)
      val signer = new StringSigner(new DsaService(Some(identityKeys.publicDsaKey), None))

      def validGuUCookieDataFor(request: RequestHeader, id: String): Option[(Cookie, GuUCookieData)] = for {
        guU <- request.cookies.get(GU_U)
        guUCookieData <- cookieDecoder.getUserDataForGuU(guU.value) if guUCookieData.user.id == id
      } yield (guU, guUCookieData)

      def secureDataFrom(scGuU: Cookie): Option[SCUCookieData] = for {
        correctlySignedString <- Try(signer.getStringForSignedString(scGuU.value)).getOrElse { logger.warn(s"Bad sig on $scGuU"); None }
        secureCookieData <- parseScGuU(correctlySignedString) if secureCookieData.expiry > clock.millis()
      } yield secureCookieData

      request => for {
        scGuU <- request.cookies.get(SC_GU_U)
        secureCookieData <- secureDataFrom(scGuU)
      } yield {
        val validGuUOpt = validGuUCookieDataFor(request, secureCookieData.id)
        AuthenticatedIdUser(
          AccessCredentials.Cookies(scGuU.value, validGuUOpt.map(_._1.value)),
          IdMinimalUser(secureCookieData.id, validGuUOpt.flatMap(_._2.user.publicFields.displayName))
        )
      }
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

