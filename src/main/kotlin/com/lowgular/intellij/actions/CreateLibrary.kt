package com.lowgular.intellij.actions

import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.lowgular.intellij.application.Analytics
import com.lowgular.intellij.application.ApiClient
import com.lowgular.intellij.infra.LOG
import com.lowgular.intellij.infra.ui.showInputDialog
import com.lowgular.intellij.infra.ui.showMessageDialog
import org.codehaus.jettison.json.JSONObject

class CreateLibrary : AbstractLowgularAction() {
    override suspend fun performAction(
        project: Project,
        file: VirtualFile,
        userId: String,
        correlationId: String,
        apiClient: ApiClient,
        analytics: Analytics
    ) {
        val name = showInputDialog(
            project, "What is the library name?",
            "Library Name"
        )
        if (name === null) {
            throw IllegalStateException("library name is required")
        }
        LOG.warn("Got name: $name")
        val payload = JSONObject(mapOf("name" to name))
        val data = apiClient.getDataObject(
            "library/create",
            payload,
        )
        showMessageDialog(
            project,
            data.getString("message"),
            "Done"
        )
        analytics.trackExtension("LibraryCreated", payload)
    }
}
