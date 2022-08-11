package com.lowgular.intellij.application

import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.Messages
import com.lowgular.intellij.LowgularSettings
import com.lowgular.intellij.coroutines.EDT
import com.lowgular.intellij.infra.AuthService
import com.lowgular.intellij.infra.LOG
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.codehaus.jettison.json.JSONObject

class Auth(val project: Project) {

    private val service = AuthService()

    // TODO: Use constructor
    suspend fun getUserId(): String {
        val storage = LowgularSettings.getService(project)
        val accessToken = storage.state.accessToken
        val refreshToken = storage.state.refreshToken
        if (accessToken === "") {
            LOG.warn("Did not find access token in storage, logging in...")
            login()
        } else {
            LOG.warn("Found access token in storage, validating...")
            if (refreshToken === "") {
                LOG.warn("Did not find refresh token in storage, logging in...")
                login()
            }
            try {
                withContext(Dispatchers.IO) {
                    service.validateToken(accessToken)
                }
                LOG.warn("Token is valid")
            } catch (e: Exception) {
                LOG.warn("Token is invalid")
                try {
                    val credentials = withContext(Dispatchers.IO) {
                        service.refreshToken(refreshToken)
                    }
                    LOG.warn("Refreshed token, $credentials")
                    storeCredentials(credentials)
                } catch (e: Exception) {
                    LOG.warn("Refresh token is invalid")
                    login()
                }
            }
        }
        return storage.state.userId
    }

    private suspend fun login() {
        while (true) {
            val email: String?
            val password: String?
            withContext(Dispatchers.EDT) {
                email = Messages.showInputDialog(
                    project, "Provide email",
                    "Email", Messages.getQuestionIcon()
                )
                password = Messages.showInputDialog(
                    project, "Provide license id",
                    "License", Messages.getQuestionIcon()
                )
            }

            if (email === null || password === null) {
                throw IllegalStateException("User has not provided email or password")
            }

            LOG.warn("Got email and password, creating credentials...")
            try {
                val credentials = withContext(Dispatchers.IO) {
                    service.createCredentials(email, password)
                }
                LOG.warn("Created credentials, $credentials")
                storeCredentials(credentials)
                break
            } catch (e: Exception) {
                LOG.warn("Error when creating the credentials, ${e.message}")
            }
        }
    }

    private fun storeCredentials(credentials: JSONObject) {
        val storage = LowgularSettings.getService(project)
        storage.setUserId(credentials.getString("id"))
        storage.setAccessToken(credentials.getString("accessToken"))
        storage.setRefreshToken(credentials.getString("refreshToken"))
        LOG.warn("Stored credentials, ${storage.state}")
    }
}
