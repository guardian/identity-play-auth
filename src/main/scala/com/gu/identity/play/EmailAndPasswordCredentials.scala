package com.gu.identity.play


case class EmailAndPasswordCredentials(email: String, password: String) {
  override def toString = s"EmailAndPasswordCredentials($email,password ${if (password.length<6) "too short" else "present"})"
}