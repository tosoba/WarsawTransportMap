package com.trm.warsawtransportmap.core.network.model

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

@Serializable data class LinesResponse(val result: Map<String, JsonObject>)
