package com.trm.warsawtransportmap.core.network.model

import kotlinx.serialization.Serializable

@Serializable data class BusesAndTramsResponse(val result: List<BusTramItem>)
