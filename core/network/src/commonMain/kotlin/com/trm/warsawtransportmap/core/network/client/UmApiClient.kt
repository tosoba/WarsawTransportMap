package com.trm.warsawtransportmap.core.network.client

import com.trm.warsawtransportmap.core.network.dto.BusesAndTramsResponse
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.post
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

class UmApiClient(private val httpClient: HttpClient) {
  suspend fun getBusesAndTrams(
    resourceId: String,
    apiKey: String,
    type: String,
  ): BusesAndTramsResponse {
    return httpClient
      .post("https://api.um.warszawa.pl/api/action/busestrams_get/") {
        url {
          parameters.append("resource_id", resourceId)
          parameters.append("apikey", apiKey)
          parameters.append("type", type)
        }
      }
      .body()
  }
}

fun umApiHttpClient(json: Json): HttpClient {
  return HttpClient { install(ContentNegotiation) { json(json) } }
}
