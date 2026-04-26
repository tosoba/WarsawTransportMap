package com.trm.warsawtransportmap.core.data

import com.trm.warsawtransportmap.core.model.Line
import com.trm.warsawtransportmap.core.model.Vehicle
import com.trm.warsawtransportmap.core.network.client.UmApiClient
import com.trm.warsawtransportmap.core.network.model.VehicleResponseItem
import com.trm.warsawtransportmap.core.network.model.VehicleType
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope

internal class TransportNetworkRepository(private val apiClient: UmApiClient) :
  TransportRepository {
  override suspend fun getVehicles(): List<Vehicle> = coroutineScope {
    val buses = async { fetchVehicles(VehicleType.BUS) }
    val trams = async { fetchVehicles(VehicleType.TRAM) }
    trams.await() + buses.await()
  }

  override suspend fun getLines(): List<Line> =
    apiClient
      .getLines()
      .result
      .keys
      .sortedWith(
        compareBy<String> { it.toIntOrNull() == null }
          .thenBy { it.toIntOrNull() ?: 0 }
          .thenBy { it }
      )
      .map(::Line)

  private suspend fun fetchVehicles(type: VehicleType): List<Vehicle> =
    apiClient.getVehicles(type).result.map(VehicleResponseItem::toDomain)
}
