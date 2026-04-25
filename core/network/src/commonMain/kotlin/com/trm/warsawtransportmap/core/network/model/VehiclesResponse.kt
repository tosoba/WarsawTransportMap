package com.trm.warsawtransportmap.core.network.model

import kotlinx.serialization.Serializable

@Serializable data class VehiclesResponse(val result: List<VehicleResponseItem>)
