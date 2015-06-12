package com.gu.identity.play

import org.scalatestplus.play._
import play.api.libs.json.{JsSuccess, Json}

class IdUserSpec extends PlaySpec {

  "parsing ID API user response" must {
    "handle a user missing their status fields" in {
      val json = Json.parse(getClass.getClassLoader.getResourceAsStream("user.without-status-fields.json"))
      val jsResult = (json \ "user").validate(IdUser.readsIdUser)

      jsResult mustBe a [JsSuccess[_]]
    }
  }
}
