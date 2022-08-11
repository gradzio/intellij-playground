package com.lowgular.intellij.actions

import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.lowgular.intellij.application.Analytics
import com.lowgular.intellij.application.ApiClient
import com.lowgular.intellij.infra.LOG
import com.lowgular.intellij.infra.ui.*
import org.codehaus.jettison.json.JSONArray
import org.codehaus.jettison.json.JSONObject

class AddMethod : AbstractLowgularAction() {

    override suspend fun performAction(
        project: Project,
        file: VirtualFile,
        userId: String,
        correlationId: String,
        apiClient: ApiClient,
        analytics: Analytics
    ) {
        val methodOptions = JSONArray().put(JSONObject().put("id", "write")).put(JSONObject().put("id", "read"))
        val selectedIndex =
            makeMultiOptionModal("What type of method is it?", "Choose Method type", methodOptions, "id")
        val methodTypeId = methodOptions.getJSONObject(selectedIndex).getString("id")

        val name = showInputDialog(
            project, "What is the method name?",
            "Method Name"
        )
        if (name === null) {
            throw IllegalStateException("Method name is required")
        }
        LOG.warn("Got name: $name")
        val csv: String? = showInputDialog(
            project, "What are the method parameters?",
            "Method Parameters"
        )
        LOG.warn("Got csv: $csv")
        val csvData = JSONObject()
        if (csv !== null && csv.isNotEmpty()) {
            val csvDataArray = csv.replace("\\s".toRegex(), "").split(",")
            for (propString in csvDataArray) {
                val keyValue = propString.split(':')
                csvData.put(keyValue[0], keyValue[1])
            }
        }

        LOG.warn("Got params: $csvData")
        when (methodTypeId) {
            "read" -> {
                val returnType = showInputDialog(
                    project, "What is the method return type?",
                    "Method Return Type"
                ) ?: throw IllegalStateException("Method return type for read method is required")

                LOG.warn("Got returnType: $returnType")
                val payload = JSONObject(
                    mapOf(
                        "entityFilePath" to file.path,
                        "name" to name,
                        "params" to csvData,
                        "returnType" to returnType
                    )
                )
                val data = apiClient.getDataObject(
                    "method-signature/add-read",
                    JSONObject(
                        mapOf(
                            "entityFilePath" to file.path,
                            "name" to name,
                            "params" to csvData,
                            "returnType" to returnType
                        )
                    ),
                )
                showMessageDialog(
                    project,
                    data.getString("message"),
                    "Done"
                )

                analytics.trackExtension("ReadMethodAdded", payload)
            }

            "write" -> {
                val payload = JSONObject(mapOf("entityFilePath" to file.path, "name" to name, "params" to csvData))
                val data = apiClient.getDataObject(
                    "method-signature/add-write",
                    JSONObject(mapOf("entityFilePath" to file.path, "name" to name, "params" to csvData)),
                )
                showMessageDialog(
                    project,
                    data.getString("message"),
                    "Done"
                )

                analytics.trackExtension("WriteMethodAdded", payload)
            }

            else -> {
                throw IllegalStateException("Unsupported method type: $methodTypeId")
            }
        }
    }
}