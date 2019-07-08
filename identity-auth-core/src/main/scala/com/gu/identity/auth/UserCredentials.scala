package com.gu.identity.auth

sealed trait UserCredentials

object UserCredentials {
  case class SCGUUCookie(value: String) extends UserCredentials
  case class CryptoAccessToken(value: String) extends UserCredentials
}
