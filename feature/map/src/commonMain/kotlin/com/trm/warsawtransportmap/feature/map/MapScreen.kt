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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TwoRowsTopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource
import org.maplibre.compose.interaction.MapInteractions
import org.maplibre.compose.map.MapState
import org.maplibre.compose.map.MaplibreMap
import warsawtransportmap.feature.map.generated.resources.Res
import warsawtransportmap.feature.map.generated.resources.app_name
import warsawtransportmap.feature.map.generated.resources.center_map_content_description
import warsawtransportmap.feature.map.generated.resources.tracking_vehicles

@Composable
fun MapTopBar(vehicleCount: Int) {
  TwoRowsTopAppBar(
    title = { Text(text = stringResource(Res.string.app_name)) },
    subtitle = {
      Text(text = pluralStringResource(Res.plurals.tracking_vehicles, vehicleCount, vehicleCount))
    },
    collapsedHeight = TopAppBarDefaults.TopAppBarExpandedHeight,
    expandedHeight = TopAppBarDefaults.TopAppBarExpandedHeight,
    windowInsets = WindowInsets(),
  )
}

@Composable
fun MapCenterVehiclesBoundingBoxFab(onClick: () -> Unit) {
  FloatingActionButton(
    containerColor = MaterialTheme.colorScheme.secondaryContainer,
    onClick = onClick,
  ) {
    Icon(
      imageVector = Icons.Default.FilterCenterFocus,
      contentDescription = stringResource(Res.string.center_map_content_description),
    )
  }
}

@Composable
fun MapPage(state: MapState, isLoadingVehicles: Boolean, modifier: Modifier = Modifier) {
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
  ) {
    AnimatedVisibility(
      visible = isLoadingVehicles,
      enter = fadeIn(),
      exit = fadeOut(),
    ) {
      LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
    }
  }
}
