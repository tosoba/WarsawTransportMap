package com.trm.warsawtransportmap.feature.map

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier

@Composable
fun MapScreen(onVehicleCountChanged: (Int) -> Unit) {
  val snackbarHostState = remember(::SnackbarHostState)
  val mapScreenState = rememberMapScreenState(snackbarHostState)

  LaunchedEffect(mapScreenState.vehicleCount) {
    onVehicleCountChanged(mapScreenState.vehicleCount)
  }

  Scaffold(
    containerColor = MaterialTheme.colorScheme.background,
    modifier = Modifier.fillMaxSize(),
    snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
    floatingActionButton = {
      MapCenterFab(onClick = mapScreenState::centerOnVehicles)
    },
  ) {
    MapScreenContent(
      state = mapScreenState,
      modifier = Modifier.fillMaxSize(),
      overlayModifier = Modifier.padding(it),
    )
  }
}
