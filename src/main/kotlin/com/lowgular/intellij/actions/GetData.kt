package com.lowgular.intellij.actions

import com.intellij.lang.javascript.service.JSLanguageServiceUtil
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.PlatformDataKeys
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.project.DumbAwareAction
import com.intellij.openapi.ui.Messages
import com.lowgular.intellij.application.ApiClient
import com.lowgular.intellij.application.Auth
import org.codehaus.jettison.json.JSONObject
import com.lowgular.intellij.infra.LOG;
import com.lowgular.intellij.infra.ui.makeMultiOptionModal
import java.util.*

class GetData : DumbAwareAction() {
    override fun actionPerformed(e: AnActionEvent) {
      val project = e.project ?: return
      val file = e.getData(PlatformDataKeys.VIRTUAL_FILE) ?: return
      val userId = Auth(project).getUserId()
      val correlationId = UUID.randomUUID().toString()
      val apiClient = ApiClient(project, userId, correlationId)

      val application = ApplicationManager.getApplication()
      application.executeOnPooledThread {
        application.invokeLater {
          try {
            val ports = apiClient.getDataArray(
              "entities/get",
              JSONObject().put("clickedPath", file.path).put("entityId", "dto-port")
            )
            val selectedPortIndex = makeMultiOptionModal("Which dto port to implement?", "Choose DTO Port", ports, "name")
            val dtoPortFile = ports.getJSONObject(selectedPortIndex).getString("path")


//            Messages.showMessageDialog(
//              project,
//              data.getString("message"),
//              "Done",
//              Messages.getInformationIcon()
//            )
          } catch (e: Error) {
            Messages.showErrorDialog(project, e.message, "Error")
          }
        }
      }
    }
}
