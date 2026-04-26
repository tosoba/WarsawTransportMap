package com.trm.warsawtransportmap.feature.map

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FilterCenterFocus
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.trm.warsawtransportmap.core.common.extensions.MapCameraAnimateToBoundingBoxEffect
import com.trm.warsawtransportmap.core.common.extensions.isAwayFrom
import com.trm.warsawtransportmap.core.common.extensions.rememberMapVehiclesBoundingBox
import kotlinx.coroutines.launch
import org.koin.compose.viewmodel.koinViewModel
import org.maplibre.compose.camera.CameraMoveReason
import org.maplibre.compose.camera.CameraPosition
import org.maplibre.compose.camera.rememberCameraState
import org.maplibre.spatialk.geojson.Position

@Composable
fun MapScreen(viewModel: MapViewModel = koinViewModel(), onNavigateToLines: () -> Unit) {
  val scope = rememberCoroutineScope()
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

  Scaffold(
    floatingActionButton = {
      Column(horizontalAlignment = Alignment.End) {
        AnimatedVisibility(showResetToBoundingBoxButton) {
          SmallFloatingActionButton(
            containerColor = MaterialTheme.colorScheme.secondaryContainer,
            onClick = { boundingBox?.let { scope.launch { cameraState.animateTo(it) } } },
          ) {
            Icon(
              imageVector = Icons.Default.FilterCenterFocus,
              contentDescription = "Center map to show all vehicles",
            )
          }
        }

        Spacer(modifier = Modifier.height(16.dp))

        FloatingActionButton(onClick = onNavigateToLines) {
          Icon(imageVector = Icons.Default.GridView, contentDescription = "Filter lines")
        }
      }
    }
  ) {
    Map(cameraState = cameraState, vehicles = vehicles)
  }
}
