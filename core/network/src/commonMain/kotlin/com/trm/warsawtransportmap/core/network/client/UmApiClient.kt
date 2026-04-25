package com.trm.warsawtransportmap.core.network.client

import com.trm.warsawtransportmap.core.network.UM_API_KEY
import com.trm.warsawtransportmap.core.network.model.LinesResponse
import com.trm.warsawtransportmap.core.network.model.VehicleType
import com.trm.warsawtransportmap.core.network.model.VehiclesResponse
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.plugins.logging.SIMPLE
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.http.appendPathSegments
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

class UmApiClient(private val httpClient: HttpClient) {
  suspend fun getVehicles(type: VehicleType): VehiclesResponse =
    httpClient
      .post(BASE_URL) {
        url {
          appendPathSegments("busestrams_get/")
          parameters.append("resource_id", "f2e5503e-927d-4ad3-9500-4ab9e55deb59")
          parameters.append("apikey", UM_API_KEY)
          parameters.append("type", type.queryParameter)
        }
      }
      .body()

  suspend fun getLines(): LinesResponse =
    httpClient
      .get(BASE_URL) {
        url {
          appendPathSegments("public_transport_routes/")
          parameters.append("apikey", UM_API_KEY)
        }
      }
      .body()

  companion object {
    private const val BASE_URL = "https://api.um.warszawa.pl/api/action"
  }
}

fun umApiHttpClient(json: Json): HttpClient = HttpClient {
  install(ContentNegotiation) { json(json) }
  install(Logging) {
    logger = Logger.SIMPLE
    level = LogLevel.ALL
  }
}
