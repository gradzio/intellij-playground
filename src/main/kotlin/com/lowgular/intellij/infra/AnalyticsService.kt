package com.lowgular.intellij.infra

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import org.codehaus.jettison.json.JSONObject

class AnalyticsService {
  val baseUrl = "https://us-central1-lowgular-extension.cloudfunctions.net"
  val client = HttpClient(CIO)
  suspend fun trackExtension(name: String, data: JSONObject, token: String) {
    val response: HttpResponse = client.post("${baseUrl}/analytics/extension") {
      contentType(ContentType.Application.Json)
      bearerAuth(token)
      setBody(JSONObject().put("data", JSONObject().put("data", data).put("name", name)).toString())
    }
    LOG.warn("Got response $response")
    if (!response.status.isSuccess()) {
      throw Exception("Error tracking extension")
    }
  }
}
