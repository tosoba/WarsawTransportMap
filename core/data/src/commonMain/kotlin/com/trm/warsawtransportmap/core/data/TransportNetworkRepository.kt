package com.trm.warsawtransportmap.core.data

import com.trm.warsawtransportmap.core.model.Vehicle
import com.trm.warsawtransportmap.core.network.client.UmApiClient
import com.trm.warsawtransportmap.core.network.model.BusTramItem

internal class TransportNetworkRepository(private val apiClient: UmApiClient) :
  TransportRepository {
  override suspend fun getVehicles(): List<Vehicle> =
    apiClient.getBusesAndTrams(RESOURCE_ID, type = 1).result.map(BusTramItem::toDomain)

  companion object {
    private const val RESOURCE_ID = "f2e5503e-927d-4ad3-9500-4ab9e55deb59"
  }
}
