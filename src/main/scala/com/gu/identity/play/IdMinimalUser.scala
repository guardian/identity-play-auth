package com.gu.identity.play

import com.gu.identity.model.{User => LegacyMutableUser}

case class IdMinimalUser(id: String, displayName: Option[String])

object IdMinimalUser {
  def from(user: LegacyMutableUser) = IdMinimalUser(user.id, user.publicFields.displayName)
  def from(user: com.gu.identity.play.IdUser) = user.minimal
}
