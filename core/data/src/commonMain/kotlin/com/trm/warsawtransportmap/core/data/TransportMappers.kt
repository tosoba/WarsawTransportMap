package com.trm.warsawtransportmap.core.data

import com.trm.warsawtransportmap.core.model.Vehicle
import com.trm.warsawtransportmap.core.network.model.BusTramItem

internal fun BusTramItem.toDomain(): Vehicle =
  Vehicle(
    lines = lines,
    longitude = lon,
    latitude = lat,
    vehicleNumber = vehicleNumber,
    time = time,
    brigade = brigade,
  )
