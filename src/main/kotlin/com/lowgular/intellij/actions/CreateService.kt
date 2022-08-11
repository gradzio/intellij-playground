package com.lowgular.intellij.actions

import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.lowgular.intellij.application.Analytics
import com.lowgular.intellij.application.ApiClient
import com.lowgular.intellij.infra.ui.makeMultiOptionModal
import com.lowgular.intellij.infra.ui.showInputDialog
import com.lowgular.intellij.infra.ui.showMessageDialog
import org.codehaus.jettison.json.JSONObject

class CreateService : AbstractLowgularAction() {

    override suspend fun performAction(
        project: Project,
        file: VirtualFile,
        userId: String,
        correlationId: String,
        apiClient: ApiClient,
        analytics: Analytics
    ) {
        val services = apiClient.getDataArray(
            "service/list",
            JSONObject(mapOf("clickedPath" to file.path)),
        )
        val selectedServiceIndex = makeMultiOptionModal("Which service to create?", "Choose Service", services, "id")
        val service = services.getJSONObject(selectedServiceIndex)
        val serviceId = service.getString("id")

        val name = showInputDialog(
            project, "What is the $serviceId name?",
            "$serviceId Name"
        )
        val payload = JSONObject(mapOf("name" to name, "clickedPath" to file.path, "entityId" to serviceId))
        val data = apiClient.getDataObject(
            "service/create",
            payload,
        )

        showMessageDialog(
            project,
            data.getString("message"),
            "Done"
        )
        analytics.trackExtension("ServiceCreated", payload)
    }
}
