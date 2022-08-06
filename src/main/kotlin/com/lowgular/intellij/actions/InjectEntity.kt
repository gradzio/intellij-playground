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
import com.lowgular.intellij.infra.ui.makeMultiOptionModal
import java.util.*

class InjectEntity : DumbAwareAction() {

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
            val entities = apiClient.getDataArray("injectable/list", JSONObject().put("entityFilePath", file.path))
            if (entities.length() == 0) {
              throw Error("Did not find any suitable entity to implement in this library...")
            }
            val selectedServiceIndex = makeMultiOptionModal("Which entity to inject?",  "Choose Entity", entities, "name")
            val selectedItem = entities.getJSONObject(selectedServiceIndex);
            val selectedItemType = selectedItem.getString("dataType")
            if (selectedItemType == "Internal") {
              val selectedFilePath = selectedItem.getString("file")

              val payload = JSONObject().put("entityFilePath", file.path).put("injectingPath", selectedFilePath).put("type", selectedItem.getString("type"))
              val data = apiClient.getDataObject("injectable/inject", payload)

              Messages.showMessageDialog(
                project,
                data.getString("message"),
                "Done",
                Messages.getInformationIcon()
              )
              analytics.trackExtension("InternalInjectableInjected", payload)
            } else if (selectedItemType == "External") {
              val payload = JSONObject().put("entityFilePath", file.path).put("externalId", selectedItem.getString("id"))
              val data = apiClient.getDataObject("injectable/inject-external", payload)

              Messages.showMessageDialog(
                project,
                data.getString("message"),
                "Done",
                Messages.getInformationIcon()
              )
              analytics.trackExtension("ExternalInjectableInjected", payload)
            } else {
              throw Error("Invalid data type: $selectedItemType")
            }

          } catch (e: Error) {
            Messages.showErrorDialog(project, e.message, "Error $correlationId")
          }
        }
      }
    }
}
