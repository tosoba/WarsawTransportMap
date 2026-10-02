package com.trm.warsawtransportmap.feature.map

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FilterCenterFocus
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TwoRowsTopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.UiComposable
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource
import org.maplibre.compose.interaction.MapInteractions
import org.maplibre.compose.map.MapState
import org.maplibre.compose.map.MaplibreMap
import org.maplibre.compose.overlay.MapOverlayScope
import warsawtransportmap.feature.map.generated.resources.Res
import warsawtransportmap.feature.map.generated.resources.app_name
import warsawtransportmap.feature.map.generated.resources.center_map_content_description
import warsawtransportmap.feature.map.generated.resources.tracking_vehicles

@Composable
fun MapView(
  state: MapState,
  modifier: Modifier = Modifier,
  overlay: @Composable @UiComposable MapOverlayScope.() -> Unit,
) {
  MaplibreMap(
    modifier = modifier,
    state = state,
    interactions =
      MapInteractions {
        camera {
          rotate { enabled = false }
          tilt { enabled = false }
        }
      },
    overlay = overlay,
  )
}

@Composable
fun MapViewVehiclesLoadingIndicator(isLoading: Boolean, modifier: Modifier = Modifier) {
  AnimatedVisibility(visible = isLoading, enter = fadeIn(), exit = fadeOut(), modifier = modifier) {
    LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
  }
}

@Composable
fun MapCenterFab(modifier: Modifier = Modifier, onClick: suspend () -> Unit) {
  val scope = rememberCoroutineScope()

  FloatingActionButton(onClick = { scope.launch { onClick() } }, modifier = modifier) {
    Icon(
      imageVector = Icons.Default.FilterCenterFocus,
      contentDescription = stringResource(Res.string.center_map_content_description),
    )
  }
}

@Composable
fun MapTopBar(vehiclesCount: Int) {
  TwoRowsTopAppBar(
    title = { Text(text = stringResource(Res.string.app_name)) },
    subtitle = {
      Text(text = pluralStringResource(Res.plurals.tracking_vehicles, vehiclesCount, vehiclesCount))
    },
    collapsedHeight = TopAppBarDefaults.TopAppBarExpandedHeight,
    expandedHeight = TopAppBarDefaults.TopAppBarExpandedHeight,
    windowInsets = WindowInsets(),
  )
}
