package io.gatling.http.krista

import io.gatling.core.config.GatlingConfiguration
import io.gatling.core.session.Expression
import io.gatling.http.protocol.HttpProtocolBuilder
import io.gatling.http.krista.request.builder.{Krista, KristaForm, KristaRequestBuilder}

trait KristaClientDsl {

  def kclient(requestName: Expression[String]) = Krista(requestName)

  def kform(formRef: Expression[String], requestName: Expression[String]) = KristaForm(formRef, requestName)
}
