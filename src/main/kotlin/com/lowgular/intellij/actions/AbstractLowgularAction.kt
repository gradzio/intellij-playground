package com.lowgular.intellij.actions

import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.PlatformDataKeys
import com.intellij.openapi.diagnostic.ControlFlowException
import com.intellij.openapi.project.DumbAwareAction
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.vfs.VirtualFile
import com.lowgular.intellij.application.Analytics
import com.lowgular.intellij.application.ApiClient
import com.lowgular.intellij.application.Auth
import com.lowgular.intellij.coroutines.EDT
import kotlinx.coroutines.*
import java.util.*

abstract class AbstractLowgularAction : DumbAwareAction() {

    private val tasksCoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        val file = e.getData(PlatformDataKeys.VIRTUAL_FILE) ?: return

        tasksCoroutineScope.launch {
            try {
                val userId = Auth(project).getUserId()
                val correlationId = UUID.randomUUID().toString()
                val apiClient = ApiClient(project, userId, correlationId)
                val analytics = Analytics(project)
                performAction(project, file, userId, correlationId, apiClient, analytics)
            } catch (e: Throwable) {
                if (e is ControlFlowException) throw e
                com.lowgular.intellij.infra.LOG.error(e)
                withContext(Dispatchers.EDT) {
                    Messages.showErrorDialog(project, e.message, "Error")
                }
            }
        }

    }

    abstract suspend fun performAction(
        project: Project, file: VirtualFile, userId: String,
        correlationId: String, apiClient: ApiClient, analytics: Analytics
    )


}