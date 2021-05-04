package io.gatling.http.krista.request.builder

import io.gatling.http
import io.gatling.http.request.builder.{CommonAttributes, HttpAttributes, HttpRequestBuilder}

case class KristaRequestBuilder(commonAttributes: CommonAttributes, httpAttributes: HttpAttributes) extends io.gatling.http.request.builder.RequestBuilder[KristaRequestBuilder] {

  private[http] def newInstance(commonAttributes: CommonAttributes): KristaRequestBuilder = new KristaRequestBuilder(commonAttributes, httpAttributes)

}
