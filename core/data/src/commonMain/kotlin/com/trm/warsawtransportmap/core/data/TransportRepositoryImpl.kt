package com.trm.warsawtransportmap.core.data

import com.trm.warsawtransportmap.core.model.Vehicle
import com.trm.warsawtransportmap.core.network.client.UmApiClient
import com.trm.warsawtransportmap.core.network.dto.BusTramItem

class TransportRepositoryImpl(private val apiClient: UmApiClient) : TransportRepository {

  override suspend fun getVehicles(): List<Vehicle> {
    val response = apiClient.getBusesAndTrams(RESOURCE_ID, type = null)
    return response.result.map { it.toDomain() }
  }

  private fun BusTramItem.toDomain() =
    Vehicle(
      lines = lines,
      longitude = lon,
      latitude = lat,
      vehicleNumber = vehicleNumber,
      time = time,
      brigade = brigade,
    )

  companion object {
    private const val RESOURCE_ID = "f2e5503e-927d-4ad3-9500-4ab9e55deb59"
  }
}
