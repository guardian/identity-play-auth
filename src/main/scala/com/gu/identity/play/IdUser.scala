package com.gu.identity.play

import play.api.libs.json.Json


case class IdUser(id: String,
                  primaryEmailAddress: String,
                  publicFields: PublicFields,
                  privateFields: Option[PrivateFields],
                  statusFields: Option[StatusFields]) {
  val minimal = IdMinimalUser(id, publicFields.displayName)
}


case class StatusFields(receiveGnmMarketing: Option[Boolean] = None,
                        receive3rdPartyMarketing: Option[Boolean] = None)

case class PublicFields(displayName: Option[String])

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

case class TelephoneNumber(countryCode: Option[String], localNumber: Option[String])

object IdUser {
  implicit val readsTelephoneNumber = Json.reads[TelephoneNumber]

  implicit val writesTelephoneNumber = Json.writes[TelephoneNumber]

  implicit val readsStatusFields = Json.reads[StatusFields]

  implicit val readsPrivateFields = Json.reads[PrivateFields]

  implicit val readsPublicFields = Json.reads[PublicFields]

  implicit val readsIdUser = Json.reads[IdUser]
}


