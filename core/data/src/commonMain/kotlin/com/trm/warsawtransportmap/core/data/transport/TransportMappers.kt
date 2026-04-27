package com.trm.warsawtransportmap.core.data.transport

import com.trm.warsawtransportmap.core.model.Vehicle
import com.trm.warsawtransportmap.core.network.model.VehicleResponseItem

internal fun VehicleResponseItem.toDomain(): Vehicle =
  Vehicle(
    lineNumber = lines,
    longitude = lon,
    latitude = lat,
    vehicleNumber = vehicleNumber,
    time = time,
    brigade = brigade,
  )
