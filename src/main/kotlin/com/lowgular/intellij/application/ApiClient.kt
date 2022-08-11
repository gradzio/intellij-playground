package com.lowgular.intellij.application

import com.intellij.lang.javascript.service.JSLanguageServiceUtil
import com.intellij.openapi.project.Project
import com.lowgular.intellij.infra.LOG
import com.lowgular.intellij.infra.grabCommandOutput
import com.lowgular.intellij.infra.makeNodeCommand
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.codehaus.jettison.json.JSONArray
import org.codehaus.jettison.json.JSONObject

class ApiClient(val project: Project, val userId: String, val correlationId: String) {
    private val cliFile = JSLanguageServiceUtil.getPluginDirectory(this::class.java, "cli/main.js").toString()
    suspend fun getDataArray(route: String, payload: JSONObject): JSONArray =
        getDataObject(route, payload)
            .getJSONArray("data")

    suspend fun getDataObject(route: String, payload: JSONObject): JSONObject =
        withContext(Dispatchers.IO) {
            grabCommandOutput(
                makeNodeCommand(
                    cliFile,
                    route,
                    payload,
                    userId,
                    project.basePath as String,
                    correlationId
                )
            )
                .also { data ->
                    LOG.warn("Got command data $data")
                    if (data.getString("status") == "error") {
                        throw Error("ID: id: $correlationId, Error: ${data.getString("message")}")
                    }
                }
        }
}

