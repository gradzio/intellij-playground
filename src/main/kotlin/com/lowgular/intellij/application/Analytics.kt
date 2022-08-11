package com.lowgular.intellij.application

import com.intellij.openapi.project.Project
import com.lowgular.intellij.LowgularSettings
import com.lowgular.intellij.infra.AnalyticsService
import com.lowgular.intellij.infra.LOG
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import org.codehaus.jettison.json.JSONObject

class Analytics(val project: Project) {
    // TODO: Is this good practice?
    private val service = AnalyticsService()

    // TODO: Use constructor
    suspend fun trackExtension(eventName: String, data: JSONObject) {
        val storage = LowgularSettings.getService(project)
        val accessToken = storage.state.accessToken
        LOG.warn("Tracking extension $eventName $data")
        coroutineScope {
            launch(Dispatchers.IO) {
                try {
                    service.trackExtension(eventName, data, accessToken)
                    LOG.warn("Token is valid")
                } catch (e: Exception) {
                    LOG.error("Token is invalid")
                }
            }
        }
    }
}
