package com.lowgular.intellij.actions

import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.PlatformDataKeys
import com.intellij.openapi.project.DumbAwareAction
import com.intellij.openapi.ui.Messages
import com.lowgular.intellij.application.ApiCaller
import com.lowgular.intellij.application.Auth
import org.codehaus.jettison.json.JSONObject
import com.lowgular.intellij.infra.LOG;

class CreateService : DumbAwareAction() {
    override fun actionPerformed(e: AnActionEvent) {
      val project = e.project ?: return
      val file = e.getData(PlatformDataKeys.VIRTUAL_FILE) ?: return
      val userId = Auth(project).getUserId()
      val name = Messages.showInputDialog(
        project, "What is the Service name?",
        "Service Name", Messages.getQuestionIcon()
      )
      if (name === null) {
        return
      }
      LOG.warn("Got name: $name")

      val options = arrayOf("http", "firebase");
      val optionIndex = Messages.showDialog(
        "What is the protocol that you want to use?",
        "Create Service", options, 0,
        Messages.getQuestionIcon()
      );
      LOG.info("Got choice ${options[optionIndex]}")
      val caller = ApiCaller();
      caller.call("service/create", JSONObject(mapOf("name" to file.path, "clickedPath" to file, "protocol" to options[optionIndex])), project, userId);
    }
}
