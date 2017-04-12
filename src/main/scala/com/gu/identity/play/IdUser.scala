package com.gu.identity.play

import play.api.libs.json.Json

case class StatusFields(receiveGnmMarketing: Option[Boolean] = None,
                        receive3rdPartyMarketing: Option[Boolean] = None)

object StatusFields {
  implicit val writesStatusFields = Json.writes[StatusFields]
  implicit val readsStatusFields = Json.reads[StatusFields]
}

case class PublicFields(displayName: Option[String])

object PublicFields {
  implicit val writesPublicFields = Json.writes[PublicFields]
  implicit val readsPublicFields = Json.reads[PublicFields]
}

case class TelephoneNumber(countryCode: Option[String], localNumber: Option[String])

object TelephoneNumber {
  implicit val writesTelephoneNumber = Json.writes[TelephoneNumber]
  implicit val readsTelephoneNumber = Json.reads[TelephoneNumber]
}

//this can't be a Map[String,String] as PrivateFields in Identity has other object types
case class PrivateFields(firstName: Option[String] = None,
                         secondName: Option[String] = None,
                         address1: Option[String] = None,
                         address2: Option[String] = None,
                         address3: Option[String] = None,
                         address4: Option[String] = None,
                         postcode: Option[String] = None,
                         country: Option[String] = None,
                         billingAddress1: Option[String] = None,
                         billingAddress2: Option[String] = None,
                         billingAddress3: Option[String] = None,
                         billingAddress4: Option[String] = None,
                         billingPostcode: Option[String] = None,
                         billingCountry: Option[String] = None,
                         socialAvatarUrl: Option[String] = None,
                         telephoneNumber: Option[TelephoneNumber] = None,
                         title: Option[String] = None
                        )

object PrivateFields {
  implicit val writesPrivateFields = Json.writes[PrivateFields]
  implicit val readsPrivateFields = Json.reads[PrivateFields]
}



case class IdUser(id: String,
  primaryEmailAddress: String,
  publicFields: PublicFields,
  privateFields: Option[PrivateFields],
  statusFields: Option[StatusFields]) {
  val minimal = IdMinimalUser(id, publicFields.displayName)
}

object IdUser {
  implicit val readsIdUser = Json.reads[IdUser]
}


