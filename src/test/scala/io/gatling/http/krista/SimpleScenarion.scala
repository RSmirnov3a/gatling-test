package io.gatling.http.krista

import io.gatling.core.Predef._
import io.gatling.core.structure.{ChainBuilder, ScenarioBuilder}
import io.gatling.http.Predef._
import io.gatling.http.krista.Predef._
import java.io.{BufferedOutputStream, File, FileOutputStream}

import scala.concurrent.duration._

object SimpleScenario {

  def getRequestCount(): Int = { 2 }

  def simpleScenario(): ChainBuilder =
    exec(
      kclient("Login").startSession("system", "masterkey")
    )
    .exec(
      kform("Report-A", "Get re-tools formId by Dynamic Form Descriptor").getFormIdByDfd("FD_Action")
    )
    .exec(
      kform("Report-A", "Open form").openForm()
    )
    .exec(
      kform("Report-A", "Fetch master details bindings").fetchMasterDetailsBindings()
    )
    .exec(
      kform("Report-A", "Load filter tree meta").loadFilterTreeMeta()
    )
    .exec(
      kform("Report-A", "Load filter tree data").loadFilterTreeData()
    )
    .exec(
      kform("Report-A", "Init Filter Panel").initPanel()
    )
    .exec(
      kform("Report-A", "Load master grid").openMasterGrid()
    )

}