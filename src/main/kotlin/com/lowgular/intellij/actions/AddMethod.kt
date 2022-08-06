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
import org.codehaus.jettison.json.JSONArray
import java.util.*

class AddMethod : DumbAwareAction() {
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
            val methodOptions = JSONArray().put(JSONObject().put("id", "write")).put(JSONObject().put("id", "read"))
            val selectedIndex = makeMultiOptionModal("What type of method is it?",  "Choose Method type", methodOptions, "id")
            val methodTypeId = methodOptions.getJSONObject(selectedIndex).getString("id")

            val name = Messages.showInputDialog(
                project, "What is the method name?",
                "Method Name", Messages.getQuestionIcon()
            )
            if (name === null) {
              throw Error("Method name is required")
            }
            LOG.warn("Got name: $name")
            val csv: String? = Messages.showInputDialog(
              project, "What are the method parameters?",
              "Method Parameters", Messages.getQuestionIcon()
            )
            LOG.warn("Got csv: $csv")
            val csvData = JSONObject();
            if (csv !== null && csv.length > 0) {
              val csvDataArray = csv.replace("\\s".toRegex(), "").split(",");
              for (propString in csvDataArray) {
                val keyValue = propString.split(':');
                csvData.put(keyValue[0], keyValue[1]);
              }
            }

            LOG.warn("Got params: ${csvData.toString()}")
            if (methodTypeId == "read") {
              val returnType = Messages.showInputDialog(
                project, "What is the method return type?",
                "Method Return Type", Messages.getQuestionIcon()
              )
              if (returnType === null) {
                throw Error("Method return type for read method is required")
              }
              LOG.warn("Got returnType: $returnType")
              val payload = JSONObject(mapOf("entityFilePath" to file.path, "name" to name, "params" to csvData, "returnType" to returnType))
              val data = apiClient.getDataObject(
                "method-signature/add-read",
                JSONObject(mapOf("entityFilePath" to file.path, "name" to name, "params" to csvData, "returnType" to returnType)),
              )
              Messages.showMessageDialog(
                project,
                data.getString("message"),
                "Done",
                Messages.getInformationIcon()
              )

              analytics.trackExtension("ReadMethodAdded", payload)
            }
            else if (methodTypeId == "write") {
              val payload = JSONObject(mapOf("entityFilePath" to file.path, "name" to name, "params" to csvData))
              val data = apiClient.getDataObject(
                "method-signature/add-write",
                JSONObject(mapOf("entityFilePath" to file.path, "name" to name, "params" to csvData)),
              )
              Messages.showMessageDialog(
                project,
                data.getString("message"),
                "Done",
                Messages.getInformationIcon()
              )

              analytics.trackExtension("WriteMethodAdded", payload)
            } else {
              throw Error("Unsupported method type: $methodTypeId");
            }
          } catch (e: Error) {
            Messages.showErrorDialog(project, e.message, "Error")
          }
        }
      }
    }
}
