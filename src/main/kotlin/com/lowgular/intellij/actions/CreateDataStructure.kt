package com.lowgular.intellij.actions

import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.lowgular.intellij.application.Analytics
import com.lowgular.intellij.application.ApiClient
import com.lowgular.intellij.infra.LOG
import com.lowgular.intellij.infra.ui.makeMultiOptionModal
import com.lowgular.intellij.infra.ui.showInputDialog
import com.lowgular.intellij.infra.ui.showMessageDialog
import org.codehaus.jettison.json.JSONObject

class CreateDataStructure : AbstractLowgularAction() {

    override suspend fun performAction(
        project: Project,
        file: VirtualFile,
        userId: String,
        correlationId: String,
        apiClient: ApiClient,
        analytics: Analytics
    ) {
        val dataStructures = apiClient.getDataArray(
            "data-structure/list",
            JSONObject(mapOf("clickedPath" to file.path)),
        )
        val selectedDataStructureIndex =
            makeMultiOptionModal("Which data structure to create?", "Choose DataStructure", dataStructures, "id")
        val dataStructure = dataStructures.getJSONObject(selectedDataStructureIndex)
        val dataStructureId = dataStructure.getString("id")

        val name = showInputDialog(
            project, "What is the $dataStructureId name?",
            "$dataStructureId Name"
        )
        if (name === null) {
            throw IllegalStateException("$dataStructureId name is required")
        }
        LOG.warn("Got name: $name")
        val propertiesCsv = showInputDialog(
            project, "What are the $dataStructureId properties?",
            "$dataStructureId Properties"
        )
        if (propertiesCsv === null) {
            throw IllegalStateException("$dataStructureId properties are required")
        }
        val props = propertiesCsv.replace("\\s".toRegex(), "").split(",")
        val properties = JSONObject()
        for (propString in props) {
            val keyValue = propString.split(':')
            properties.put(keyValue[0], keyValue[1])
        }
        LOG.warn("Got props: $properties")
        val payload = JSONObject(
            mapOf(
                "entityId" to dataStructureId,
                "name" to name,
                "clickedPath" to file.path,
                "properties" to properties
            )
        )
        val data = apiClient.getDataObject(
            "data-structure/create",
            payload,
        )
        showMessageDialog(
            project,
            data.getString("message"),
            "Done"
        )
        analytics.trackExtension("DataStructureCreated", payload)
    }
}
