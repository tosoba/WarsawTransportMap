package com.trm.warsawtransportmap.feature.map

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.trm.warsawtransportmap.core.common.extensions.toErrorMessage
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.koin.compose.viewmodel.koinViewModel

data class MapScreenState(
  val vehicleCount: Int,
  val isLoadingVehicles: Boolean,
  val mapVehiclesState: MapVehiclesState,
) {
  suspend fun centerOnVehicles() {
    mapVehiclesState.animateToVehiclesBoundingBoxOrDefault()
  }
}

@Composable
fun rememberMapScreenState(snackbarHostState: SnackbarHostState): MapScreenState {
  val scope = rememberCoroutineScope()
  val currentSnackbarHostState by rememberUpdatedState(snackbarHostState)

  val mapViewModel = koinViewModel<MapViewModel>()
  val vehicles by mapViewModel.vehicles.collectAsStateWithLifecycle()
  val isLoadingVehicles by mapViewModel.isLoadingVehicles.collectAsStateWithLifecycle()
  val initialCameraPosition by
    mapViewModel.initialCameraPosition.collectAsStateWithLifecycle(initialValue = null)

  val mapViewState =
    rememberMapVehiclesState(
      vehicles = vehicles,
      initialCameraPosition = initialCameraPosition,
      onCameraPositionChange = mapViewModel::onSavedCameraPositionChange,
      onVehicleClick = { vehicle ->
        scope.launch {
          val message = vehicle.toUpdateTimeMessage() ?: return@launch
          currentSnackbarHostState.currentSnackbarData?.dismiss()
          currentSnackbarHostState.showSnackbar(message)
        }
      },
    )

  LaunchedEffect(mapViewModel.errors, snackbarHostState) {
    mapViewModel.errors.collectLatest { error ->
      snackbarHostState.currentSnackbarData?.dismiss()
      snackbarHostState.showSnackbar(message = error.toErrorMessage())
    }
  }

  return MapScreenState(
    vehicleCount = vehicles.size,
    isLoadingVehicles = isLoadingVehicles,
    mapVehiclesState = mapViewState,
  )
}
