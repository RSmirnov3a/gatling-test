package io.gatling.http.krista.request.builder

import io.gatling.core.session.Expression
import io.gatling.core.structure.ChainBuilder
import io.gatling.http.Predef._
import io.gatling.core.Predef._
import GatlingHelper._
import com.eatthepath.uuid.FastUUID
import io.gatling.core.action.builder.FeedBuilder
import jodd.typeconverter.impl.CollectionConverter
import collection.JavaConverters._

case class KristaForm(formRef: Expression[String], requestName: Expression[String]) {

  def findConsolidationReportFormById(id: Expression[String]): ChainBuilder = {
    exec()
      .exec(
        http(requestName)
          .post("application/CallExtensionController")
          .basicAuth("${kclient_user}", "${kclient_password}")
          .formParam(
            "jsonData",
            template("{\"dataType\":\"ru.krista.consolidation.controller.WebGetReportFormDescriptorData\",\"inputParams\":[{\"reportId\":\"${1}\"}]}",
              id
            ))
          .check(status.is(200))
          .check(jsonPath("$[0].uiFormId").exists.saveAs("kclient_selected_form"))
          .check(
              checkResponse((response, khelper) =>
              khelper.form(deref(khelper, formRef)).init(deref(khelper, id), deref(khelper, "${kclient_selected_form}")))
          )
      )
  }

  def openForm(): ChainBuilder = {
    exec()
      .exec(loadToSession("kclient_selected_form", khelper => khelper.form(deref(khelper, formRef)).getFormId()))
      .exec(
        http(concat(requestName, " (${kclient_selected_form})"))
          .post("application/JsonInterface?formId=${kclient_selected_form}")
          .basicAuth("${kclient_user}", "${kclient_password}")
          .check(status.is(200))
          .check(jsonPath("$.instanceCode").exists.saveAs("kclient_selected_instanceCode"))
          .check(checkResponse((response, khelper) => khelper.form(deref(khelper, formRef)).onFormOpen(response, deref(khelper, "${kclient_selected_instanceCode}"))))
      )
  }

  def getFormIdByDfd(dfdName: Expression[String]): ChainBuilder = {
    exec()
      .exec(
        http(requestName)
          .get(template("application/HelperFunctionsServlet?function=getFormIdByName&formName=${1}", dfdName))
          .basicAuth("${kclient_user}", "${kclient_password}")
          .check(status.is(200))
          .check(jsonPath("$.value").exists.saveAs("kclient_selected_form"))
          .check(
            checkResponse((response, khelper) =>
              khelper.form(deref(khelper, formRef)).init(deref(khelper, dfdName), deref(khelper, "${kclient_selected_form}")))
          )
      )
  }

  def fetchMasterDetailsBindings(): ChainBuilder = {
    exec()
      .exec(loadToSession("kclient_selected_form", khelper => khelper.form(deref(khelper, formRef)).getFormId()))
      .exec(loadToSession("kclient_selected_instanceCode", khelper => khelper.form(deref(khelper, formRef)).getInstanceId()))
      .exec(
        http(requestName)
          .post("application/masterdetail?formId=${kclient_selected_form}&form=${kclient_selected_instanceCode}")
          .basicAuth("${kclient_user}", "${kclient_password}")
          .check(status.is(200))
          .check(substring("").notExists) // fake checker to prevent body disposal
          .check(checkResponse((respose, khelper) => khelper.form(deref(khelper, formRef)).loadMasterFormBindings(respose)))
      )
  }

  def loadFilterTreeMeta(): ChainBuilder = {
    exec()
      .exec(loadToSession("kclient_filter_tree", khelper => khelper.form(deref(khelper, formRef)).getFilterTreeId()))
      .exec(loadToSession("kclient_selected_instanceCode", khelper => khelper.form(deref(khelper, formRef)).getInstanceId()))
      .exec(
        http(concat(requestName, " (${kclient_filter_tree})"))
          .post("application/filtertree")
          .basicAuth("${kclient_user}", "${kclient_password}")
          .formParam("action", "fetch")
          .formParam("linkID", "${kclient_filter_tree}")
          .formParam("form", "${kclient_selected_instanceCode}")
          .check(status.is(200))
          .check(substring("").notExists)
      )
  }
  def loadFilterTreeData(): ChainBuilder = {
    exec()
      .exec(loadToSession("kclient_selected_instanceCode", khelper => khelper.form(deref(khelper, formRef)).getInstanceId()))
      .exec(
        http(concat(requestName, " (${kclient_filter_tree})"))
          .post("application/filtertree")
          .basicAuth("${kclient_user}", "${kclient_password}")
          .formParam("action", "fetch")

          .formParam("className", "ru.krista.rpn.model.lab.request.Request")
          .formParam("fieldNamesList", "Цель испытаний, Документ - основание")
          .formParam("fieldName", "Цель испытаний")
          .formParam("isRoot", "true")
          .formParam("useAllItem", "true")
          .formParam("source", "targetResearch.name")

          .formParam("linkID", "${kclient_filter_tree}")
          .formParam("form", "${kclient_selected_instanceCode}")
          .check(status.is(200))
          .check(substring("").notExists) // fake checker to prevent body disposal
          .check(checkResponse((respose, khelper) => khelper.form(deref(khelper, formRef)).loadMasterFormBindings(respose)))
      )
  }

  def initPanel(): ChainBuilder = {
    exec()
      .exec(loadToSession("kclient_panel_name", khelper => khelper.form(deref(khelper, formRef)).getPanelName()))
      .exec(loadToSession("kclient_selected_instanceCode", khelper => khelper.form(deref(khelper, formRef)).getInstanceId()))
      .exec(
        http(requestName)
          .post("application/panel/")
          .basicAuth("${kclient_user}", "${kclient_password}")
          .formParam("form", "${kclient_panel_name}")
          .formParam("name", "${kclient_selected_instanceCode}")
          .formParam("operation", "itit")
          .check(status.is(200))
          .check(substring("").notExists) // fake checker to prevent body disposal
      )
  }

  def openMasterGrid(): ChainBuilder = {
    exec()
      .exec(loadToSession("kclient_currentMasterGridBinding", khelper => khelper.form(deref(khelper, formRef)).getGridId()))
      .exec(loadToSession("kclient_selected_instanceCode", khelper => khelper.form(deref(khelper, formRef)).getInstanceId()))
      .exec(
        http(requestName)
          .post("application/grid/${kclient_currentMasterGridBinding}.load")
          .basicAuth("${kclient_user}", "${kclient_password}")
          .formParam("pageSize", "50")
          .formParam("form", "${kclient_selected_instanceCode}")
          .formParam("order", "")
          .formParam("transitParams", "{}")
          .formParam("action", "fetch")
          .formParam("forceUpdate", "1")
          //.formParam("filter", "{\"_className\":\"Filter\",\"conditions\":[\"{\\\"_className\\\":\\\"Filter\\\",\\\"conditions\\\":[]}\",{}]}")
          .formParam("filter", "{\"_className\":\"Filter\",\"conditions\":[{\"_className\":\"ComplexCondition\",\"conditions\":[],\"operation\":\"and\"},{\"_className\":\"Filter\",\"conditions\":[{\"_className\":\"Filter\",\"conditions\":[{\"_className\":\"ComplexCondition\",\"conditions\":[{\"_className\":\"Relation\",\"leftOperand\":{\"_className\":\"EntityAttribute\",\"stringPath\":\"uniqueCode\"},\"rightOperand\":{\"_className\":\"ObjectConst\",\"type\":\"BigDecimal\",\"value\":\"125.00000000\"},\"operation\":\">\"}],\"operation\":\"or\"}]},{}]},{\"_className\":\"Filter\",\"conditions\":[{\"_className\":\"ComplexCondition\",\"conditions\":[],\"operation\":\"and\"}]}]}")
          .check(status.is(200))
          .check(substring("").notExists) // fake checker to prevent body disposal
      )
  }

  def fetchTabs(): ChainBuilder = {
    exec()
      .exec(loadToSession("kclient_reportClass", khelper => khelper.form(deref(khelper, formRef)).getReportClass()))
      .exec(loadToSession("kclient_currentMasterGridBinding", khelper => khelper.form(deref(khelper, formRef)).getMasterGridBinding()))
      .exec(loadToSession("kclient_selected_instanceCode", khelper => khelper.form(deref(khelper, formRef)).getInstanceId()))
      // fetch tabs
      .exec(
        http(requestName)
          .post("application/CallExtensionController?form=${kclient_selected_instanceCode}")
          .basicAuth("${kclient_user}", "${kclient_password}")
          .formParam("jsonData", "{\"dataType\":\"ru.krista.consolidation.controller.CheckDataAvailableInSectionsData\",\"inputParams\":[{\"reportId\":\"${kclient_reportClass}\"}]}")
          .check(status.is(200))
          .check(substring("").notExists) // fake checker to prevent body disposal
          .check(checkResponse((respose, khelper) => khelper.form(deref(khelper, formRef)).updateTabsDataAvailability(respose)))
      )
  }

  def forEachTab(bindingVar: String)(chain: ChainBuilder): ChainBuilder = {
    exec()
      .exec(loadToSession("kclient_binding_list", khelper =>  khelper.form(deref(khelper, formRef)).listTabBindingsWithData().asScala))
      .foreach("${kclient_binding_list}", bindingVar){exec(chain)}
  }

  def openTab(tabBinding: Expression[KClientHelper.TabInfo]): ChainBuilder = {
    exec()
      .exec(session => session.set("kclient_datasetId", tabBinding.apply(session).toOption.get.dataset))
      .exec(loadToSession("kclient_masterKey", khelper => khelper.form(deref(khelper, formRef)).getMasterKey()))
      .exec(loadToSession("kclient_selected_instanceCode", khelper => khelper.form(deref(khelper, formRef)).getInstanceId()))
      .exec(
        http(requestName)
          .post("application/dataset")
          .basicAuth("${kclient_user}", "${kclient_password}")
          .formParam("bldKey", "${kclient_datasetId}")
          .formParam("masterKey", "${kclient_masterKey}")
          .formParam("form", "${kclient_selected_instanceCode}")
          .check(status.is(200))
          .check(jsonPath("$[0].key").exists.saveAs("kclient_currentDataset"))
      )
      .exec(session => session.set("kclient_filterId", tabBinding.apply(session).toOption.get.filter))
      .exec(
        http(requestName)
          .post("application/propertyfilter/${kclient_filterId}.defaultfilter")
          .basicAuth("${kclient_user}", "${kclient_password}")
          .formParam("bldId", "${kclient_filterId}")
          .formParam("action", "defaultfilter")
          .formParam("form", "${kclient_selected_instanceCode}")
          .check(status.is(200))
          .check(substring("").notExists) // fake checker to prevent body disposal
      )
      .exec(session => session.set("kclient_gridId", tabBinding.apply(session).toOption.get.grid))
      .exec(
        http(requestName)
          .post("application/grid/${kclient_gridId}-${kclient_currentDataset}.load")
          .basicAuth("${kclient_user}", "${kclient_password}")
          .formParam("pageSize", "100")
          .formParam("order", "")
          .formParam("form", "${kclient_selected_instanceCode}")
          .formParam("transitParams", "{}")
          .formParam("action", "fetch")
          .formParam("forceUpdate", "1")
          .formParam("filter", "{\"_className\":\"Filter\",\"conditions\":[]}")
          .check(status.is(200))
      )
  }
}
