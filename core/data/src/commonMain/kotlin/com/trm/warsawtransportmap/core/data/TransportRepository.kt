package com.trm.warsawtransportmap.core.data

import com.trm.warsawtransportmap.core.model.Vehicle

interface TransportRepository {
  suspend fun getVehicles(): List<Vehicle>
}
