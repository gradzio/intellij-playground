package com.lowgular.intellij.infra

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import org.codehaus.jettison.json.JSONObject

class AuthService {
  private val baseUrl = "https://us-central1-lowgular-extension.cloudfunctions.net"
  private val client = HttpClient(CIO)

  suspend fun createCredentials(email: String, password: String): JSONObject {
    val response: HttpResponse = client.post("${baseUrl}/auth/login") {
      contentType(ContentType.Application.Json)
      setBody(JSONObject().put("data", JSONObject().put("email", email).put("password", password)).toString())
    }
    if (!response.status.isSuccess()) {
      throw Exception("Invalid Credentials")
    }
    val respJson = JSONObject(response.body() as String)
    return respJson.getJSONObject("data")
  }

    suspend fun refreshToken(token: String): JSONObject {
    val response: HttpResponse = client.post("${baseUrl}/auth/refresh") {
      contentType(ContentType.Application.Json)
      setBody(JSONObject().put("data", JSONObject().put("refreshToken", token)).toString())
    }
    if (!response.status.isSuccess()) {
      throw Exception("Invalid refresh token")
    }
    val respJson = JSONObject(response.body() as String)
    return respJson.getJSONObject("data")
  }

  suspend fun validateToken(token: String) {

    val response: HttpResponse = client.get("${baseUrl}/auth/me") {
      contentType(ContentType.Application.Json)
      bearerAuth(token)
    }
    LOG.warn("Sent $token received $response.status.value")
    if (!response.status.isSuccess()) {
      throw Exception("Invalid or expired access token")
    }
  }
}
