package com.gu.identity.play

import java.net.InetAddress

import com.google.common.net.InetAddresses
import play.api.http.HeaderNames._
import play.api.mvc.RequestHeader

import scala.util.Try

// TODO: are these utility methods really needed in this library? Consider removing.

object ProxiedIP {
  def getIP(request: RequestHeader): Option[InetAddress] = for {
    xFor <- request.headers.get(X_FORWARDED_FOR)
    ipString <- xFor.split(",").headOption if ipString.nonEmpty
    ip <- Try(InetAddresses.forString(ipString)).toOption
  } yield ip
}