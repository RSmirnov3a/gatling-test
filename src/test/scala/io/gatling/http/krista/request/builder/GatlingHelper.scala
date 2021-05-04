package io.gatling.http.krista.request.builder

import io.gatling.commons.validation.{Success, Validation}
import io.gatling.core.Predef._
import io.gatling.core.check.{Check, CheckResult}
import io.gatling.core.session.{Expression, Session}
import io.gatling.http.Predef.Response
import io.gatling.http.check.{HttpCheck, HttpCheckScope}

object GatlingHelper {

  def concat(expr1: Expression[String], expr2: Expression[String]): Expression[String] = {
    session => {
      deref(session, expr1) + deref(session, expr2)
    }
  }

  def template(template: Expression[String], param: Expression[String]):  Expression[String] = {
    session => {
      deref(session.set("1", deref(session, param)), template)
    }
  }

  def deref(session: Session, expr: Expression[String]): String = {
    expr.apply(session).toOption.get
  }

  type ResponseCheckerFunction = (Response, KClientHelper) => Validation[Unit]

  case class ResponseChecker(handler: ResponseCheckerFunction) extends Check[Response] {

    override def check(response: Response, session: Session)(implicit preparedCache: java.util.Map[Any, Any]): Validation[CheckResult] = {
      implicit val khelper = KClientHelper.resolve(session)
      val ok = handler.apply(response, khelper)

      ok.map(u => CheckResult(Some(khelper), Some(KClientHelper.KCLIENT_VAR)))
    }
  }

  def checkResponse(handler: ResponseCheckerFunction): HttpCheck = {
    return HttpCheck(ResponseChecker(handler), HttpCheckScope.Body, None)
  }

  type Extractor = KClientHelper => Any

  def loadToSession(varName: String, handler: Extractor): Expression[Session] = {
    session => {
      val kclient = KClientHelper.resolve(session)
      val value = handler.apply(kclient)
      kclient.getLatestSessionState().set(varName, value)
    }
  }

  def deref(khelper: KClientHelper, expr: Expression[String]): String = {
    expr.apply(khelper.getLatestSessionState()).toOption.get
  }
}
