//package com.gu.identity.play.idapi
//
//import com.gu.identity.play.{PrivateFields, PublicFields, StatusFields}
//import play.api.libs.json.Json
//
//case class CreateIdUser(
//  primaryEmailAddress: String,
//  password: String,
//  publicFields: PublicFields,
//  privateFields: Option[PrivateFields] = None,
//  statusFields: Option[StatusFields] = None) {
//}
//
//object CreateIdUser {
//  implicit val writesCreateIdUser = Json.writes[CreateIdUser]
//}