package com.trm.warsawtransportmap.feature.map

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FilterCenterFocus
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TwoRowsTopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.trm.warsawtransportmap.core.common.extensions.toErrorMessage
import com.trm.warsawtransportmap.core.model.Vehicle
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import org.jetbrains.compose.resources.getPluralString
import org.jetbrains.compose.resources.getString
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.maplibre.compose.interaction.MapInteractions
import org.maplibre.compose.map.MapState
import org.maplibre.compose.map.MaplibreMap
import warsawtransportmap.feature.map.generated.resources.Res
import warsawtransportmap.feature.map.generated.resources.app_name
import warsawtransportmap.feature.map.generated.resources.center_map_content_description
import warsawtransportmap.feature.map.generated.resources.filter_lines_content_description
import warsawtransportmap.feature.map.generated.resources.select_lines
import warsawtransportmap.feature.map.generated.resources.tracking_vehicles
import warsawtransportmap.feature.map.generated.resources.vehicle_updated_minutes_ago
import warsawtransportmap.feature.map.generated.resources.vehicle_updated_now
import warsawtransportmap.feature.map.generated.resources.vehicle_updated_seconds_ago
import kotlin.time.Clock

@Composable
fun MapScreen(viewModel: MapViewModel = koinViewModel(), onNavigateToLines: () -> Unit) {
  val scope = rememberCoroutineScope()
  val snackbarHostState = remember(::SnackbarHostState)

  val vehicles by viewModel.vehicles.collectAsStateWithLifecycle()
  val isLoadingVehicles by viewModel.isLoadingVehicles.collectAsStateWithLifecycle()
  val initialCameraPosition by
    viewModel.initialCameraPosition.collectAsStateWithLifecycle(initialValue = null)
  val errors = viewModel.errors

  LaunchedEffect(errors) {
    errors.collectLatest { error ->
      snackbarHostState.currentSnackbarData?.dismiss()
      snackbarHostState.showSnackbar(message = error.toErrorMessage())
    }
  }

  val (state, boundingBox) =
    rememberMapScreenState(
      vehicles = vehicles,
      initialCameraPosition = initialCameraPosition,
      onCameraPositionChange = viewModel::onCameraPositionChange,
      onVehicleClick = { vehicle ->
        scope.launch {
          val message = vehicle.toUpdateTimeMessage() ?: return@launch
          snackbarHostState.currentSnackbarData?.dismiss()
          snackbarHostState.showSnackbar(message)
        }
      },
    )

  Scaffold(
    topBar = {
      TwoRowsTopAppBar(
        title = { Text(text = stringResource(Res.string.app_name)) },
        subtitle = {
          Text(
            text = pluralStringResource(Res.plurals.tracking_vehicles, vehicles.size, vehicles.size)
          )
        },
        collapsedHeight = TopAppBarDefaults.TopAppBarExpandedHeight,
        expandedHeight = TopAppBarDefaults.TopAppBarExpandedHeight,
        windowInsets = WindowInsets(),
      )
    },
    snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
    floatingActionButton = {
      Column(horizontalAlignment = Alignment.End) {
        FloatingActionButton(
          containerColor = MaterialTheme.colorScheme.secondaryContainer,
          onClick = { boundingBox?.let { scope.launch { state.animateCameraToBounds(it) } } },
        ) {
          Icon(
            imageVector = Icons.Default.FilterCenterFocus,
            contentDescription = stringResource(Res.string.center_map_content_description),
          )
        }

        Spacer(modifier = Modifier.height(16.dp))

        ExtendedFloatingActionButton(
          onClick = onNavigateToLines,
          icon = {
            Icon(
              imageVector = Icons.Default.GridView,
              contentDescription = stringResource(Res.string.filter_lines_content_description),
            )
          },
          text = { Text(stringResource(Res.string.select_lines)) },
        )
      }
    },
  ) { paddingValues ->
    MapCanvas(state = state, isLoadingVehicles = isLoadingVehicles, paddingValues = paddingValues)
  }
}

@Composable
private fun MapCanvas(state: MapState, isLoadingVehicles: Boolean, paddingValues: PaddingValues) {
  MaplibreMap(
    modifier = Modifier.fillMaxSize(),
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
      modifier = Modifier.padding(paddingValues),
    ) {
      LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
    }
  }
}

private suspend fun Vehicle.toUpdateTimeMessage(): String? = runCatching {
  val duration =
    Clock.System.now() -
      LocalDateTime.parse(time.replace(" ", "T")).toInstant(TimeZone.currentSystemDefault())
  when {
    duration.inWholeMinutes > 0 -> {
      getPluralString(
        Res.plurals.vehicle_updated_minutes_ago,
        duration.inWholeMinutes.toInt(),
        duration.inWholeMinutes,
      )
    }
    duration.inWholeSeconds > 10 -> {
      getPluralString(
        Res.plurals.vehicle_updated_seconds_ago,
        duration.inWholeSeconds.toInt(),
        duration.inWholeSeconds,
      )
    }
    else -> {
      getString(Res.string.vehicle_updated_now)
    }
  }
}
  .getOrNull()
