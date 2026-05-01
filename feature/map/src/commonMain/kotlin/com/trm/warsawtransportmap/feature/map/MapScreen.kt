package com.trm.warsawtransportmap.feature.map

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FilterCenterFocus
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SmallFloatingActionButton
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
import com.trm.warsawtransportmap.core.common.extensions.MapCameraAnimateToBoundingBoxEffect
import com.trm.warsawtransportmap.core.common.extensions.rememberMapVehiclesBoundingBox
import io.ktor.client.plugins.ResponseException
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.io.IOException
import org.jetbrains.compose.resources.getString
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.maplibre.compose.camera.CameraMoveReason
import org.maplibre.compose.camera.CameraPosition
import org.maplibre.compose.camera.rememberCameraState
import org.maplibre.spatialk.geojson.Position
import warsawtransportmap.feature.map.generated.resources.Res
import warsawtransportmap.feature.map.generated.resources.app_name
import warsawtransportmap.feature.map.generated.resources.center_map_content_description
import warsawtransportmap.feature.map.generated.resources.error_http
import warsawtransportmap.feature.map.generated.resources.error_network
import warsawtransportmap.feature.map.generated.resources.error_unknown
import warsawtransportmap.feature.map.generated.resources.filter_lines_content_description
import warsawtransportmap.feature.map.generated.resources.select_lines
import warsawtransportmap.feature.map.generated.resources.tracking_vehicles

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun MapScreen(viewModel: MapViewModel = koinViewModel(), onNavigateToLines: () -> Unit) {
  val scope = rememberCoroutineScope()
  val snackbarHostState = remember(::SnackbarHostState)

  val vehicles by viewModel.vehicles.collectAsStateWithLifecycle()
  val isLoadingVehicles by viewModel.isLoadingVehicles.collectAsStateWithLifecycle()

  val initialCameraPosition by
    viewModel.initialCameraPosition.collectAsStateWithLifecycle(initialValue = null)
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
  val boundingBox = rememberMapVehiclesBoundingBox(vehicles = vehicles, percentageIncrease = 0.1)

  LaunchedEffect(cameraState.isCameraMoving) {
    if (!cameraState.isCameraMoving) {
      viewModel.onCameraPositionChange(cameraState.position)
    }
  }

  initialCameraPosition?.let {
    LaunchedEffect(Unit) {
      cameraState.animateTo(
        CameraPosition(
          target = Position(latitude = it.latitude, longitude = it.longitude),
          zoom = it.zoom,
        )
      )
    }
  }
    ?: run {
      if (
        cameraState.moveReason == CameraMoveReason.NONE ||
          cameraState.moveReason == CameraMoveReason.PROGRAMMATIC
      ) {
        MapCameraAnimateToBoundingBoxEffect(boundingBox, cameraState)
      }
    }

  LaunchedEffect(Unit) {
    viewModel.errors.collectLatest { error ->
      snackbarHostState.showSnackbar(message = error.toErrorMessage())
    }
  }

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
        AnimatedVisibility(visible = vehicles.isNotEmpty()) {
          SmallFloatingActionButton(
            containerColor = MaterialTheme.colorScheme.secondaryContainer,
            onClick = { boundingBox?.let { scope.launch { cameraState.animateTo(it) } } },
          ) {
            Icon(
              imageVector = Icons.Default.FilterCenterFocus,
              contentDescription = stringResource(Res.string.center_map_content_description),
            )
          }
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
    Box(modifier = Modifier.padding(paddingValues)) {
      Map(cameraState = cameraState, vehicles = vehicles)

      AnimatedVisibility(visible = isLoadingVehicles, enter = fadeIn(), exit = fadeOut()) {
        LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
      }
    }
  }
}

private suspend fun Throwable.toErrorMessage(): String =
  when (this) {
    is IOException -> getString(Res.string.error_network)
    is ResponseException -> getString(Res.string.error_http, response.status.value)
    else -> getString(Res.string.error_unknown)
  }
