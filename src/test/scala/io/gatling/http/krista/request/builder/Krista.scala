package io.gatling.http.krista.request.builder

import io.gatling.commons.validation.Validation
import io.gatling.core.session.Session
import io.gatling.core.session.Expression
import io.gatling.http.client.uri.Uri
import io.gatling.http.check.{HttpCheck, HttpCheckScope}
import io.gatling.http.request.builder.{CommonAttributes, HttpAttributes, HttpRequestBuilder}
import io.netty.handler.codec.http.HttpMethod
import io.gatling.core.Predef._
import io.gatling.core.check.Check
import io.gatling.core.check.CheckResult
import io.gatling.core.structure.ChainBuilder
import io.gatling.http.Predef._
import GatlingHelper._

case class Krista(requestName: Expression[String]) {

  def startSession(user: Expression[String], password: Expression[String]): ChainBuilder = {
    exec()
    .exec{session => session.set("kclient_user", deref(session, user))}
    .exec{session => session.set("kclient_password", deref(session, password))}
    .exec(
      http(requestName)
        .get("application/jsonformmanager")
        .basicAuth("${kclient_user}", "${kclient_password}")
        .check(status.is(200))
    )
  }
}