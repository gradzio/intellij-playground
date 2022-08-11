package com.lowgular.intellij.actions

import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.lowgular.intellij.application.Analytics
import com.lowgular.intellij.application.ApiClient
import com.lowgular.intellij.infra.ui.makeMultiOptionModal
import com.lowgular.intellij.infra.ui.showMessageDialog
import org.codehaus.jettison.json.JSONObject

class ImplementEntity : AbstractLowgularAction() {

    override suspend fun performAction(
        project: Project,
        file: VirtualFile,
        userId: String,
        correlationId: String,
        apiClient: ApiClient,
        analytics: Analytics
    ) {
        val entities = apiClient.getDataArray("implementable/list", JSONObject().put("entityFilePath", file.path))
        if (entities.length() == 0) {
            throw IllegalStateException("Did not find any suitable entities to inject in this library...")
        }
        val selectedIndex = makeMultiOptionModal("Which entity to inject?", "Choose Entity", entities, "name")
        val selectedItem = entities.getJSONObject(selectedIndex)
        val selectedFilePath = selectedItem.getString("file")

        val payload = JSONObject().put("entityFilePath", file.path).put("abstractionFilePath", selectedFilePath)
        val data = apiClient.getDataObject("implementable/implement", payload)

        showMessageDialog(
            project,
            data.getString("message"),
            "Done"
        )
        analytics.trackExtension("ImplementableImplemented", payload)
    }
}
