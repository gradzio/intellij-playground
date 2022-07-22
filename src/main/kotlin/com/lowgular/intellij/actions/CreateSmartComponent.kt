package com.lowgular.intellij.actions

import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.PlatformDataKeys
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.project.DumbAwareAction
import com.intellij.openapi.ui.Messages
import com.lowgular.intellij.application.ApiClient
import com.lowgular.intellij.application.Auth
import org.codehaus.jettison.json.JSONObject
import com.lowgular.intellij.infra.LOG;
import java.util.*

class CreateSmartComponent : DumbAwareAction() {
    override fun actionPerformed(e: AnActionEvent) {
      val project = e.project ?: return
      val file = e.getData(PlatformDataKeys.VIRTUAL_FILE) ?: return
      val userId = Auth(project).getUserId()
      val correlationId = UUID.randomUUID().toString()
      val apiClient = ApiClient(project, userId, correlationId)

      val name = Messages.showInputDialog(
        project, "What is the Component name?",
        "Component Name", Messages.getQuestionIcon()
      )
      if (name === null) {
        return
      }
      LOG.warn("Got name: $name")
      val application = ApplicationManager.getApplication()
      application.executeOnPooledThread {
        application.invokeLater {
          try {
            val data = apiClient.getDataObject(
            "smart-component/create",
            JSONObject(mapOf("name" to name, "clickedPath" to file.path)),
          )
            Messages.showMessageDialog(
              project,
            data.getString("message"),
            "Done",
            Messages.getInformationIcon()
            )
          } catch (e: Error) {
            Messages.showErrorDialog(project, e.message, "Error");
          }
        }
      }
    }
}
