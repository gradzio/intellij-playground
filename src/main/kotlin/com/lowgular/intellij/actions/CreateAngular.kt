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

class CreateAngular : AbstractLowgularAction() {

    override suspend fun performAction(
        project: Project,
        file: VirtualFile,
        userId: String,
        correlationId: String,
        apiClient: ApiClient,
        analytics: Analytics
    ) {
        val abstractions = apiClient.getDataArray(
            "angular/list",
            JSONObject(mapOf("clickedPath" to file.path)),
        )
        val selectedIndex = makeMultiOptionModal("Which angular to create?", "Choose Angular", abstractions, "id")
        val abstractionId = abstractions.getJSONObject(selectedIndex).getString("id")

        val name = showInputDialog(
            project, "What is the $abstractionId name?",
            "$abstractionId Name"
        )
        if (name === null) {
            throw IllegalStateException("$abstractionId name is required")
        }
        LOG.warn("Got name: $name")
        val payload = JSONObject(mapOf("entityId" to abstractionId, "name" to name, "clickedPath" to file.path))
        val data = apiClient.getDataObject(
            "angular/create",
            payload,
        )
        showMessageDialog(
            project,
            data.getString("message"),
            "Done"
        )
        analytics.trackExtension("AngularCreated", payload)
    }
}
