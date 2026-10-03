package com.trm.warsawtransportmap.feature.map

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.trm.warsawtransportmap.core.common.extensions.toErrorMessage
import com.trm.warsawtransportmap.core.model.Vehicle
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
  val currentSnackbarHostState by rememberUpdatedState(snackbarHostState)

  val mapViewModel = koinViewModel<MapViewModel>()
  val vehicles by mapViewModel.vehicles.collectAsStateWithLifecycle()
  val isLoadingVehicles by mapViewModel.isLoadingVehicles.collectAsStateWithLifecycle()
  val initialCameraPosition by
    mapViewModel.initialCameraPosition.collectAsStateWithLifecycle(initialValue = null)

  var clickedVehicle by remember { mutableStateOf<Vehicle?>(null) }
  val clickedVehicleMessage = clickedVehicle?.toUpdateTimeMessage()

  val mapViewState =
    rememberMapVehiclesState(
      vehicles = vehicles,
      initialCameraPosition = initialCameraPosition,
      onCameraPositionChange = mapViewModel::onSavedCameraPositionChange,
      onVehicleClick = { vehicle -> clickedVehicle = vehicle },
    )

  LaunchedEffect(clickedVehicle, clickedVehicleMessage) {
    val message = clickedVehicleMessage ?: return@LaunchedEffect
    currentSnackbarHostState.currentSnackbarData?.dismiss()
    currentSnackbarHostState.showSnackbar(message)
    clickedVehicle = null
  }

  val error by mapViewModel.errors.collectAsStateWithLifecycle(initialValue = null)
  val errorMessage = error?.toErrorMessage()

  LaunchedEffect(error, errorMessage) {
    val message = errorMessage ?: return@LaunchedEffect
    currentSnackbarHostState.currentSnackbarData?.dismiss()
    currentSnackbarHostState.showSnackbar(message = message)
  }

  return MapScreenState(
    vehicleCount = vehicles.size,
    isLoadingVehicles = isLoadingVehicles,
    mapVehiclesState = mapViewState,
  )
}
