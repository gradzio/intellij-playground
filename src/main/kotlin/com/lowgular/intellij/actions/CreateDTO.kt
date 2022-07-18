package com.lowgular.intellij.actions

import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.PlatformDataKeys
import com.intellij.openapi.project.DumbAwareAction
import com.intellij.openapi.ui.Messages
import com.lowgular.intellij.application.ApiCaller
import com.lowgular.intellij.application.Auth
import org.codehaus.jettison.json.JSONObject
import com.lowgular.intellij.infra.LOG;

class CreateDTO : DumbAwareAction() {
    override fun actionPerformed(e: AnActionEvent) {
      val project = e.project ?: return
      val file = e.getData(PlatformDataKeys.VIRTUAL_FILE) ?: return
      val userId = Auth(project).getUserId()
      LOG.warn("Project base path ${project.basePath}")
      LOG.warn("User id: $userId")
      val name = Messages.showInputDialog(
          project, "What is the DTO name?",
          "DTO Name", Messages.getQuestionIcon()
      )
      if (name === null) {
        return
      }
      LOG.warn("Got name: $name")
      val propertiesCsv = Messages.showInputDialog(
        project, "What are the properties?",
        "DTO Properties", Messages.getQuestionIcon()
      )
      if (propertiesCsv === null) {
        return
      }
      val props = propertiesCsv.replace("\\s".toRegex(), "").split(",");
      val properties = JSONObject();
      for (propString in props) {
        val keyValue = propString.split(':');
        properties.put(keyValue[0], keyValue[1]);
      }
      LOG.warn("Got props: ${properties.toString()}")
      val apiCaller = ApiCaller()
      apiCaller.call("dto/create", JSONObject(mapOf("name" to name, "clickedPath" to file.path, "properties" to properties)), project, userId);
    }
}
