package com.lowgular.intellij.actions

import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.PlatformDataKeys
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.project.DumbAwareAction
import com.intellij.openapi.ui.Messages
import com.lowgular.intellij.application.Analytics
import com.lowgular.intellij.application.ApiClient
import com.lowgular.intellij.application.Auth
import org.codehaus.jettison.json.JSONObject
import com.lowgular.intellij.infra.LOG;
import com.lowgular.intellij.infra.ui.makeMultiOptionModal
import java.util.*

class CreateService : DumbAwareAction() {
    override fun actionPerformed(e: AnActionEvent) {
      val project = e.project ?: return
      val file = e.getData(PlatformDataKeys.VIRTUAL_FILE) ?: return
      val userId = Auth(project).getUserId()
      val correlationId = UUID.randomUUID().toString()
      val apiClient = ApiClient(project, userId, correlationId)
      val analytics = Analytics(project)

      val application = ApplicationManager.getApplication()
      application.executeOnPooledThread {
        application.invokeLater {
          try {
            val services = apiClient.getDataArray(
              "service/list",
              JSONObject(mapOf("clickedPath" to file.path)),
            )
            val selectedServiceIndex = makeMultiOptionModal("Which service to create?",  "Choose Service", services, "id")
            val service = services.getJSONObject(selectedServiceIndex)
            val serviceId = service.getString("id")

            val name = Messages.showInputDialog(
              project, "What is the $serviceId name?",
              "$serviceId Name", Messages.getQuestionIcon()
            )
            val payload = JSONObject(mapOf("name" to name, "clickedPath" to file.path, "entityId" to serviceId))
            val data = apiClient.getDataObject(
              "service/create",
              payload,
            )

          Messages.showMessageDialog(
            project,
            data.getString("message"),
            "Done",
            Messages.getInformationIcon()
          )
            analytics.trackExtension("ServiceCreated", payload)
        } catch (e: Error) {
        Messages.showErrorDialog(project, e.message, "Error");
      }
        }
      }
    }
}
