package com.lowgular.intellij.application

import com.intellij.lang.javascript.service.JSLanguageServiceUtil
import com.intellij.openapi.project.Project
import com.lowgular.intellij.infra.LOG
import com.lowgular.intellij.infra.grabCommandOutput
import com.lowgular.intellij.infra.makeNodeCommand
import org.codehaus.jettison.json.JSONArray
import org.codehaus.jettison.json.JSONObject

class ApiClient(val project: Project, val userId: String, val correlationId: String) {
  val cliFile = JSLanguageServiceUtil.getPluginDirectory(this::class.java, "cli/main.js")
  fun getDataArray(route: String, payload: JSONObject): JSONArray {
      val cliFile = JSLanguageServiceUtil.getPluginDirectory(this::class.java, "cli/main.js")
      val command = makeNodeCommand(cliFile.toString(), route, payload, userId, project.basePath as String, correlationId)
      val data = grabCommandOutput(command)
      LOG.warn("Got command data ${data.toString()}")
      if (data.getString("status") == "error") {
        throw Error("ID: id: $correlationId, Error Getting the data for route: $route")
      }
      return data.getJSONArray("data")
  }
  fun getDataObject(route: String, payload: JSONObject): JSONObject {
    val command = makeNodeCommand(cliFile.toString(), route, payload, userId, project.basePath as String, correlationId)
    val data = grabCommandOutput(command)
    if (data.getString("status") == "error") {
      throw Error("ID: id: $correlationId, Error: ${data.getString("message")}")
    }
    return data
  }
}

