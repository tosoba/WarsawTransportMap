package com.trm.warsawtransportmap.core.common.extensions

import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

fun calculateDistanceBetweenKm(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
  val r = 6371.0 // Earth's radius in km
  val dLat = (lat2 - lat1) * (PI / 180.0)
  val dLon = (lon2 - lon1) * (PI / 180.0)
  val a =
    sin(dLat / 2) * sin(dLat / 2) +
      cos(lat1 * (PI / 180.0)) * cos(lat2 * (PI / 180.0)) * sin(dLon / 2) * sin(dLon / 2)
  val c = 2 * atan2(sqrt(a), sqrt(1 - a))
  return r * c
}
