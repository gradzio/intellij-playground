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

class CreateDtoPort : DumbAwareAction() {
    override fun actionPerformed(e: AnActionEvent) {
      val project = e.project ?: return
      val file = e.getData(PlatformDataKeys.VIRTUAL_FILE) ?: return
      val userId = Auth(project).getUserId()
      val correlationId = UUID.randomUUID().toString()
      val apiClient = ApiClient(project, userId, correlationId)

      val options = arrayOf("getAll", "getOne", "add", "set", "remove");
      val optionIndex = Messages.showDialog(
          "What is the feature that you want to add?",
          "Create DTO Port", options, 0,
        Messages.getQuestionIcon()
      );
      LOG.info("Got choice ${options[optionIndex]}")
      val application = ApplicationManager.getApplication()
      application.executeOnPooledThread {
        application.invokeLater {
          try {
            val data = apiClient.getDataObject(
            "dto-port/create",
            JSONObject(mapOf("dtoFile" to file.path, "crudMethod" to options[optionIndex])),
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
