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

class CreateAbstraction : DumbAwareAction() {
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
            val abstractions = apiClient.getDataArray(
              "abstraction/list",
              JSONObject(mapOf("clickedPath" to file.path)),
            )
            val selectedIndex = makeMultiOptionModal("Which abstraction to create?",  "Choose Abstraction", abstractions, "id")
            val abstractionId = abstractions.getJSONObject(selectedIndex).getString("id")

            val name = Messages.showInputDialog(
                project, "What is the $abstractionId name?",
                "$abstractionId Name", Messages.getQuestionIcon()
            )
            if (name === null) {
              throw Error("$abstractionId name is required")
            }
            LOG.warn("Got name: $name")
            val payload = JSONObject(mapOf("entityId" to abstractionId, "name" to name, "dataStructureFile" to file.path))
            val data = apiClient.getDataObject(
              "abstraction/create",
              payload,
            )
            Messages.showMessageDialog(
              project,
              data.getString("message"),
              "Done",
              Messages.getInformationIcon()
            )
            analytics.trackExtension("AbstractionCreated", payload)
          } catch (e: Error) {
            Messages.showErrorDialog(project, e.message, "Error")
          }
        }
      }
    }
}
