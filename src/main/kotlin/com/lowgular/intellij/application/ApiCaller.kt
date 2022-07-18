package com.lowgular.intellij.application

import com.intellij.lang.javascript.service.JSLanguageServiceUtil
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.Messages
import com.lowgular.intellij.infra.grabCommandOutput
import com.lowgular.intellij.infra.makeNodeCommand
import org.codehaus.jettison.json.JSONObject

class ApiCaller {
  fun call(route: String, payload: JSONObject, project: Project, userId: String) {
    val application = ApplicationManager.getApplication()
    application.executeOnPooledThread {
      val cliFile = JSLanguageServiceUtil.getPluginDirectory(this::class.java, "cli/main.js")
      val command = makeNodeCommand(cliFile.toString(), route, payload, userId, project.basePath as String)
      val data = grabCommandOutput(command)
      application.invokeLater {
        if (data.getString("status") == "error") {
          Messages.showErrorDialog(project, data.getString("message"), "Error");
        }
        if (data.getString("status") == "ok") {
          Messages.showMessageDialog(
            project,
            data.getString("message"),
            "Done",
            Messages.getInformationIcon()
          )
        }
      }
    }
  }
}

