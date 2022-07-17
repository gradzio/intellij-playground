package com.lowgular.intellij.application

import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.Messages
import com.lowgular.intellij.LowgularSettings
import com.lowgular.intellij.infra.AuthService
import com.lowgular.intellij.infra.LOG
import kotlinx.coroutines.runBlocking

class Auth(val project: Project) {
  // TODO: Is this good practice?
  val service = AuthService()
  // TODO: Use constructor
  fun getUserId(): String {
    val storage = LowgularSettings.getService(project);
    val accessToken = storage.state.accessToken
    if (accessToken === "")  {
      LOG.warn("Did not find access token in storage, logging in...")
      login()
    } else {
      LOG.warn("Found access token in storage, validating...")
      runBlocking {
        try {
          service.validateToken(accessToken);
          LOG.warn("Token is valid")
        } catch (e: Exception) {
          LOG.warn("Token is invalid")
          login();
        }
      }
    }
    return storage.state.userId
  }

  private fun login() {
    val email = Messages.showInputDialog(
      project, "Provide email",
      "Email", Messages.getQuestionIcon()
    )
    val password = Messages.showInputDialog(
      project, "Provide license id",
      "License", Messages.getQuestionIcon()
    )

    if (email === null || password === null) {
      login()
    }

    LOG.warn("Got email and password, creating credentials...")
    // TODO: is this fine?
    runBlocking {
      try {
        val credentials = service.createCredentials(email as String, password as String)
        LOG.warn("Created credentials, ${credentials.toString()}")
        val storage = LowgularSettings.getService(project)
        storage.setUserId(credentials.getString("id"))
        storage.setAccessToken(credentials.getString("accessToken"))
        LOG.warn("Stored credentials, ${storage.state.toString()}")
      } catch (e: Exception) {
        LOG.warn("Error when creating the credentials, ${e.message}")
        login()
      }

    }
  }
}
