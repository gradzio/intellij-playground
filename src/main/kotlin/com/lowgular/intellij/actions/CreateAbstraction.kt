package com.lowgular.intellij.actions

import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.lowgular.intellij.application.Analytics
import com.lowgular.intellij.application.ApiClient
import com.lowgular.intellij.infra.LOG
import com.lowgular.intellij.infra.ui.*
import org.codehaus.jettison.json.JSONObject

class CreateAbstraction : AbstractLowgularAction() {
    override suspend fun performAction(
        project: Project,
        file: VirtualFile,
        userId: String,
        correlationId: String,
        apiClient: ApiClient,
        analytics: Analytics
    ) {
        val abstractions = apiClient.getDataArray(
            "abstraction/list",
            JSONObject(mapOf("clickedPath" to file.path)),
        )
        val selectedIndex =
            makeMultiOptionModal("Which abstraction to create?", "Choose Abstraction", abstractions, "id")
        val abstractionId = abstractions.getJSONObject(selectedIndex).getString("id")

        val name = showInputDialog(
          project, "What is the $abstractionId name?",
          "$abstractionId Name"
        )
        if (name === null) {
            throw IllegalStateException("$abstractionId name is required")
        }
        LOG.warn("Got name: $name")
        val payload = JSONObject(mapOf("entityId" to abstractionId, "name" to name, "dataStructureFile" to file.path))
        val data = apiClient.getDataObject(
            "abstraction/create",
            payload,
        )
        showMessageDialog(
          project,
          data.getString("message"),
          "Done"
        )
        analytics.trackExtension("AbstractionCreated", payload)
    }
}
