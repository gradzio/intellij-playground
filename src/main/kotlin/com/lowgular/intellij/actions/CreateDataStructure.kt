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

class CreateDataStructure : DumbAwareAction() {
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
            val dataStructures = apiClient.getDataArray(
              "data-structure/list",
              JSONObject(mapOf("clickedPath" to file.path)),
            )
            val selectedDataStructureIndex = makeMultiOptionModal("Which data structure to create?",  "Choose DataStructure", dataStructures, "id")
            val dataStructure = dataStructures.getJSONObject(selectedDataStructureIndex)
            val dataStructureId = dataStructure.getString("id")

            val name = Messages.showInputDialog(
                project, "What is the $dataStructureId name?",
                "$dataStructureId Name", Messages.getQuestionIcon()
            )
            if (name === null) {
              throw Error("$dataStructureId name is required")
            }
            LOG.warn("Got name: $name")
            val propertiesCsv = Messages.showInputDialog(
              project, "What are the $dataStructureId properties?",
              "$dataStructureId Properties", Messages.getQuestionIcon()
            )
            if (propertiesCsv === null) {
              throw Error("$dataStructureId properties are required")
            }
            val props = propertiesCsv.replace("\\s".toRegex(), "").split(",");
            val properties = JSONObject();
            for (propString in props) {
              val keyValue = propString.split(':');
              properties.put(keyValue[0], keyValue[1]);
            }
            LOG.warn("Got props: ${properties.toString()}")
            val payload = JSONObject(mapOf("entityId" to dataStructureId, "name" to name, "clickedPath" to file.path, "properties" to properties))
            val data = apiClient.getDataObject(
              "data-structure/create",
              payload,
            )
            Messages.showMessageDialog(
              project,
              data.getString("message"),
              "Done",
              Messages.getInformationIcon()
            )
            analytics.trackExtension("DataStructureCreated", payload)
          } catch (e: Error) {
            Messages.showErrorDialog(project, e.message, "Error")
          }
        }
      }
    }
}
