package com.trm.warsawtransportmap.core.common.extensions

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import com.trm.warsawtransportmap.core.model.Vehicle
import org.maplibre.compose.camera.CameraState
import org.maplibre.spatialk.geojson.BoundingBox

@Composable
fun rememberMapVehiclesBoundingBox(
  vehicle: List<Vehicle>,
  percentageIncrease: Double,
): BoundingBox? =
  remember(vehicle, percentageIncrease) {
    if (vehicle.isEmpty()) return@remember null

    val minLat = vehicle.minOf(Vehicle::latitude)
    val maxLat = vehicle.maxOf(Vehicle::latitude)
    val minLon = vehicle.minOf(Vehicle::longitude)
    val maxLon = vehicle.maxOf(Vehicle::longitude)

    val latDelta = maxLat - minLat
    val lonDelta = maxLon - minLon

    val paddingFactor = percentageIncrease / 2.0

    BoundingBox(
      west = minLon - lonDelta * paddingFactor,
      south = minLat - latDelta * paddingFactor,
      east = maxLon + lonDelta * paddingFactor,
      north = maxLat + latDelta * paddingFactor,
    )
  }

@Composable
fun MapCameraAnimateToBoundingBoxEffect(
  boundingBox: BoundingBox?,
  cameraState: CameraState,
  padding: PaddingValues = PaddingValues.Zero,
) {
  LaunchedEffect(boundingBox, padding) {
    if (boundingBox != null) {
      cameraState.animateTo(boundingBox = boundingBox, padding = padding)
    }
  }
}
