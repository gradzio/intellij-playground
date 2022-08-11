package com.lowgular.intellij.actions

import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.lowgular.intellij.application.Analytics
import com.lowgular.intellij.application.ApiClient
import com.lowgular.intellij.infra.ui.makeMultiOptionModal
import com.lowgular.intellij.infra.ui.showMessageDialog
import org.codehaus.jettison.json.JSONObject

class InjectEntity : AbstractLowgularAction() {
    override suspend fun performAction(
        project: Project,
        file: VirtualFile,
        userId: String,
        correlationId: String,
        apiClient: ApiClient,
        analytics: Analytics
    ) {
        val entities = apiClient.getDataArray("injectable/list", JSONObject().put("entityFilePath", file.path))
        if (entities.length() == 0) {
            throw IllegalStateException("Did not find any suitable entity to implement in this library...")
        }
        val selectedServiceIndex = makeMultiOptionModal("Which entity to inject?", "Choose Entity", entities, "name")
        val selectedItem = entities.getJSONObject(selectedServiceIndex)
        when (val selectedItemType = selectedItem.getString("dataType")) {
            "Internal" -> {
                val selectedFilePath = selectedItem.getString("file")

                val payload = JSONObject().put("entityFilePath", file.path).put("injectingPath", selectedFilePath)
                    .put("type", selectedItem.getString("type"))
                val data = apiClient.getDataObject("injectable/inject", payload)

                showMessageDialog(
                    project,
                    data.getString("message"),
                    "Done"
                )
                analytics.trackExtension("InternalInjectableInjected", payload)
            }

            "External" -> {
                val payload =
                    JSONObject().put("entityFilePath", file.path).put("externalId", selectedItem.getString("id"))
                val data = apiClient.getDataObject("injectable/inject-external", payload)

                showMessageDialog(
                    project,
                    data.getString("message"),
                    "Done"
                )
                analytics.trackExtension("ExternalInjectableInjected", payload)
            }

            else -> throw IllegalStateException("Invalid data type: $selectedItemType")

        }
    }
}
