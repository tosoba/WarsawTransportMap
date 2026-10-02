package com.trm.warsawtransportmap.feature.map

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun MapScreenContent(
  state: MapScreenState,
  modifier: Modifier = Modifier,
  overlayModifier: Modifier = Modifier,
) {
  MapView(
    state = state.mapVehiclesState.mapState,
    modifier = modifier,
  ) {
    MapViewVehiclesLoadingIndicator(
      isLoading = state.isLoadingVehicles,
      modifier = overlayModifier,
    )
  }
}
