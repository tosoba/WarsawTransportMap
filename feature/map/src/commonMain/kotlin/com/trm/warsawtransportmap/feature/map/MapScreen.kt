package com.trm.warsawtransportmap.feature.map

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.trm.warsawtransportmap.core.common.extensions.MapCameraAnimateToBoundingBoxEffect
import com.trm.warsawtransportmap.core.common.extensions.isAwayFrom
import com.trm.warsawtransportmap.core.common.extensions.rememberMapVehiclesBoundingBox
import org.koin.compose.viewmodel.koinViewModel
import org.maplibre.compose.camera.CameraMoveReason
import org.maplibre.compose.camera.CameraPosition
import org.maplibre.compose.camera.rememberCameraState
import org.maplibre.spatialk.geojson.Position

@Composable
fun MapScreen(viewModel: MapViewModel = koinViewModel()) {
  val vehicles by viewModel.vehicles.collectAsStateWithLifecycle()
  val firstPosition = remember {
    CameraPosition(
      target =
        Position(
          latitude = MapConstants.WARSAW_CENTER_LAT,
          longitude = MapConstants.WARSAW_CENTER_LON,
        )
    )
  }
  val cameraState = rememberCameraState(firstPosition = firstPosition)
  val boundingBox = rememberMapVehiclesBoundingBox(vehicles, percentageIncrease = 0.1)
  val center =
    remember(boundingBox) {
      boundingBox?.let {
        Position(longitude = (it.west + it.east) / 2.0, latitude = (it.south + it.north) / 2.0)
      }
    }
  val showResetToBoundingBoxButton =
    remember(cameraState.position.target, center) {
      center != null &&
        cameraState.position.target.isAwayFrom(
          latitude = center.latitude,
          longitude = center.longitude,
        )
    }

  if (
    cameraState.moveReason == CameraMoveReason.NONE ||
      cameraState.moveReason == CameraMoveReason.PROGRAMMATIC
  ) {
    MapCameraAnimateToBoundingBoxEffect(boundingBox, cameraState)
  }

  Map(cameraState = cameraState, vehicles = vehicles)
}
