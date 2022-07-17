package com.lowgular.intellij.actions

import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.PlatformDataKeys
import com.intellij.openapi.project.DumbAwareAction
import com.intellij.openapi.ui.Messages
import com.lowgular.intellij.application.ApiCaller
import com.lowgular.intellij.application.Auth
import org.codehaus.jettison.json.JSONObject
import com.lowgular.intellij.infra.LOG;

class CreateSmartComponent : DumbAwareAction() {
    override fun actionPerformed(e: AnActionEvent) {
      val project = e.project ?: return
      val file = e.getData(PlatformDataKeys.VIRTUAL_FILE) ?: return
      val userId = Auth(project).getUserId()
      val name = Messages.showInputDialog(
        project, "What is the Component name?",
        "Component Name", Messages.getQuestionIcon()
      )
      if (name === null) {
        return
      }
      LOG.warn("Got name: $name")

      val caller = ApiCaller()
      caller.call("smart-component/create", JSONObject(mapOf("name" to file.path, "clickedPath" to file)), project, userId)
    }
}
