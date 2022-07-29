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
import com.lowgular.intellij.infra.LOG
import com.lowgular.intellij.infra.grabCommandOutput
import com.lowgular.intellij.infra.makeNodeCommand
import com.lowgular.intellij.infra.ui.makeMultiOptionModal
import java.util.*

class ImplementEntity : DumbAwareAction() {

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
            val entities = apiClient.getDataArray("implementable/list", JSONObject().put("entityFilePath", file.path))
            if (entities.length() == 0) {
              throw Error("Did not find any entities in this library...")
            }
            val selectedIndex = makeMultiOptionModal("Which entity to inject?",  "Choose Entity", entities, "name")
            val selectedItem = entities.getJSONObject(selectedIndex);
            val selectedFilePath = selectedItem.getString("file")

            val data = apiClient.getDataObject("implementable/implement", JSONObject().put("entityFilePath", file.path).put("abstractionFilePath", selectedFilePath))

            Messages.showMessageDialog(
              project,
              data.getString("message"),
              "Done",
              Messages.getInformationIcon()
            )
          } catch (e: Error) {
            Messages.showErrorDialog(project, e.message, "Error $correlationId")
          }
        }
      }
    }
}
