package com.gu.identity.play

/**
 * @param authCookie - the "SC_GU_U" cookie the user authenticated with
 */
case class AuthenticatedIdUser(authCookie: String, user: IdMinimalUser)