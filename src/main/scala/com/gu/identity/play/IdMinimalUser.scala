package com.gu.identity.play

import com.gu.identity.model.{User => LegacyMutableUser}

// TODO: c.f. comment on AuthenticatedIdUser class; remove IdMinimalUser

case class IdMinimalUser(id: String, displayName: Option[String])

object IdMinimalUser {
  def from(user: LegacyMutableUser) = IdMinimalUser(user.id, user.publicFields.displayName)
  def from(user: com.gu.identity.play.IdUser) = user.minimal
}
