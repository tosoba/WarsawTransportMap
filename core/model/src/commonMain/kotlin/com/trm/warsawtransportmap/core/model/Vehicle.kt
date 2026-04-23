package com.trm.warsawtransportmap.core.model

import kotlinx.serialization.Serializable

@Serializable
enum class VehicleType {
  BUS,
  TRAM,
}

@Serializable
data class Vehicle(
  val lineNumber: String,
  val longitude: Double,
  val latitude: Double,
  val vehicleNumber: String,
  val time: String,
  val brigade: String,
  val type: VehicleType? = null,
)
