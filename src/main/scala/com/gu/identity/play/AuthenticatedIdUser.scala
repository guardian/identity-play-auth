package com.gu.identity.play

import com.github.nscala_time.time.Imports._
import com.gu.identity.cookie.{IdentityCookieDecoder, IdentityKeys}
import com.gu.identity.model.{CryptoAccessToken, LiftJsonConfig, User}
import com.gu.identity.play.AuthenticatedIdUser.Provider
import com.gu.identity.signing.{CollectionSigner, DsaService, StringSigner}
import org.joda.time.DateTime
import play.api.mvc.RequestHeader
import com.github.nscala_time.time.Imports._

case class AuthenticatedIdUser(credentials: AccessCredentials, user: IdMinimalUser)

object AuthenticatedIdUser {
  type Provider = RequestHeader => Option[AuthenticatedIdUser]

  def minimalUserFor(user: User) = IdMinimalUser(user.id, user.publicFields.displayName)

  def provider(providers: Provider*) = {
    r : RequestHeader => providers.flatMap(_(r)).headOption
  }
}

sealed trait AccessCredentials

object AccessCredentials {
  case class Cookies(scGuU: String, guU: String) extends AccessCredentials

  object Cookies {
    def authProvider(identityKeys: IdentityKeys): Provider = {
      val cookieDecoder = new IdentityCookieDecoder(identityKeys)

      request => for {
        scGuU <- request.cookies.get("SC_GU_U")
        guU <- request.cookies.get("GU_U")
        minimalSecureUser <- cookieDecoder.getUserDataForScGuU(scGuU.value)
        guUCookieData <- cookieDecoder.getUserDataForGuU(guU.value)
        user = guUCookieData.user if user.id == minimalSecureUser.id
      } yield AuthenticatedIdUser(
        AccessCredentials.Cookies(scGuU.value, guU.value),
        IdMinimalUser.from(user)
      )
    }
  }

  case class Token(tokenText: String) extends AccessCredentials

  object Token {
    /** @param targetClientId Not confidential, eg "members-data-api" https://github.com/guardian/identity-token-auth-sample/blob/e640832d/main.scala#L28
      */
    def authProvider(identityKeys: IdentityKeys, targetClientId: String): Provider = {
      val collectionSigner = new CollectionSigner(new StringSigner(new DsaService(identityKeys.publicDsaKey, null)), LiftJsonConfig.formats)

      def extractUserDataFromToken(tokenString: String) = {
        val cryptoAccessToken = collectionSigner.getValueForSignedStringJava(tokenString, classOf[CryptoAccessToken])

        if (cryptoAccessToken.expiryTime < DateTime.now) {
          Left(s"Token: $tokenString has expired")
        } else if (cryptoAccessToken.targetClient != targetClientId) {
          Left(s"Token: $tokenString was not targeted for the client '$targetClientId'")
        } else Right(cryptoAccessToken.getUser)
      }

      request => for {
        tokenText <- request.headers.get("GU-IdentityToken")
        user <- extractUserDataFromToken(tokenText).right.toOption
      } yield AuthenticatedIdUser(
        AccessCredentials.Token(tokenText),
        IdMinimalUser.from(user)
      )
    }
  }
}

