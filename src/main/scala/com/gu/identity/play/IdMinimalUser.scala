package com.gu.identity.play

import com.gu.identity.model.User

case class IdMinimalUser(id: String, displayName: Option[String])

object IdMinimalUser {
  def from(user: User) = IdMinimalUser(user.id, user.publicFields.displayName)
}
