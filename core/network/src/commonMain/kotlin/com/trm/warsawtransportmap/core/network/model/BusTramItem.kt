package com.trm.warsawtransportmap.core.network.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class BusTramItem(
  @SerialName("Lines") val lines: String,
  @SerialName("Lon") val lon: Double,
  @SerialName("Lat") val lat: Double,
  @SerialName("VehicleNumber") val vehicleNumber: String,
  @SerialName("Time") val time: String,
  @SerialName("Brigade") val brigade: String,
)
