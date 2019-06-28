package com.gu.identity.play.idapi

import com.gu.identity.play.{PrivateFields, PublicFields, StatusFields}
import play.api.libs.json.Json

// TODO: remove this file;
// not responsibility of identity-play-auth to provide case classes that model identity API requests / responses

case class UpdateIdUser(
  primaryEmailAddress: Option[String] = None,
  publicFields: Option[PublicFields] = None,
  privateFields: Option[PrivateFields] = None,
  statusFields: Option[StatusFields] = None) {
}

object UpdateIdUser {
  implicit val writesUpdateIdUser = Json.writes[UpdateIdUser]
}