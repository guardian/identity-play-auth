package com.gu.identity.auth

import com.gu.identity.model.{PrivateFields, PublicFields, TelephoneNumber, User}
import io.circe.Decoder

private trait UserDecoder {

  // FIXME
  implicit val userDecoder: Decoder[User] = ???

//  {
//    implicit val telephoneNumberDecoder: Decoder[TelephoneNumber] = deriveDecoder[TelephoneNumber]
//    implicit val privateFieldsDecoder: Decoder[PrivateFields] = deriveDecoder[PrivateFields]
//    implicit val publicFieldsDecoder: Decoder[PublicFields] = deriveDecoder[PublicFields]
//    deriveDecoder[User]
//  }
}
