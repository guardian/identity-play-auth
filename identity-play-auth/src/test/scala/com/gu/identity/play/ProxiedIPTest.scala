package com.gu.identity.play

import com.google.common.net.InetAddresses
import org.scalatest.{Matchers, FlatSpec}

import play.api.test.FakeRequest

class ProxiedIPTest extends FlatSpec with Matchers {
  "ProxiedIP" should "extract the first ip" in {
    ProxiedIP.getIP(FakeRequest().withHeaders("X-Forwarded-For" -> "77.91.250.233, 77.91.250.233, 185.31.18.24")) should be(Some(InetAddresses.forString("77.91.250.233")))
  }

  "ProxiedIP" should "not crash if XForwardedFor is not present" in {
    assert(ProxiedIP.getIP(FakeRequest()).isEmpty)
  }
  "ProxiedIP" should "return none for empty XForwardedFor" in {
    assert(ProxiedIP.getIP(FakeRequest().withHeaders("X-Forwarded-For" -> "")).isEmpty)
  }
  "ProxiedIP" should "not crash if XForwardedFor is not an ip address" in {
    assert(ProxiedIP.getIP(FakeRequest().withHeaders("X-Forwarded-For" -> "notanip")).isEmpty)
  }

}
