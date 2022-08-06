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

class CreateObject : DumbAwareAction() {
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
            val objects = apiClient.getDataArray(
              "object/list",
              JSONObject(mapOf("clickedPath" to file.path)),
            )
            val selectedObjectIndex = makeMultiOptionModal("Which object to create?",  "Choose Object", objects, "id")
            val objectId = objects.getJSONObject(selectedObjectIndex).getString("id")

            val name = Messages.showInputDialog(
                project, "What is the $objectId name?",
                "$objectId Name", Messages.getQuestionIcon()
            )
            if (name === null) {
              throw Error("$objectId name is required")
            }
            LOG.warn("Got name: $name")
            val propertiesCsv = Messages.showInputDialog(
              project, "What are the $objectId properties?",
              "$objectId Properties", Messages.getQuestionIcon()
            )
            if (propertiesCsv === null) {
              throw Error("$objectId properties are required")
            }
            val props = propertiesCsv.replace("\\s".toRegex(), "").split(",");
            val properties = JSONObject();
            for (propString in props) {
              val keyValue = propString.split(':');
              properties.put(keyValue[0], keyValue[1]);
            }
            LOG.warn("Got props: ${properties.toString()}")
            val payload = JSONObject(mapOf("entityId" to objectId, "name" to name, "clickedPath" to file.path, "properties" to properties))
            val data = apiClient.getDataObject(
              "object/create",
              payload,
            )
            Messages.showMessageDialog(
              project,
              data.getString("message"),
              "Done",
              Messages.getInformationIcon()
            )
            analytics.trackExtension("ObjectCreated", payload)
          } catch (e: Error) {
            Messages.showErrorDialog(project, e.message, "Error")
          }
        }
      }
    }
}
