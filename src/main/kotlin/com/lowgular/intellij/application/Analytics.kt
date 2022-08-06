package com.lowgular.intellij.application

import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.Messages
import com.lowgular.intellij.LowgularSettings
import com.lowgular.intellij.infra.AnalyticsService
import com.lowgular.intellij.infra.AuthService
import com.lowgular.intellij.infra.LOG
import io.ktor.client.*
import kotlinx.coroutines.runBlocking
import org.codehaus.jettison.json.JSONObject

class Analytics(val project: Project) {
  // TODO: Is this good practice?
  val service = AnalyticsService()
  // TODO: Use constructor
  fun trackExtension(eventName: String, data: JSONObject) {
    val storage = LowgularSettings.getService(project);
    val accessToken = storage.state.accessToken
    LOG.warn("Tracking extension $eventName $data")
      runBlocking {
        try {
          service.trackExtension(eventName, data, accessToken)
          LOG.warn("Token is valid")
        } catch (e: Exception) {
          LOG.error("Token is invalid")
        }
      }
    }
}
