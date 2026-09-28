package com.trm.warsawtransportmap.feature.map

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.trm.warsawtransportmap.core.common.extensions.MapCameraAnimateToBoundingBoxEffect
import com.trm.warsawtransportmap.core.common.extensions.rememberMapVehiclesBoundingBox
import com.trm.warsawtransportmap.core.common.extensions.toErrorMessage
import com.trm.warsawtransportmap.core.model.Vehicle
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.decodeFromJsonElement
import org.jetbrains.compose.resources.getPluralString
import org.jetbrains.compose.resources.getString
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.maplibre.compose.camera.CameraMoveReason
import org.maplibre.compose.camera.CameraUpdate
import org.maplibre.compose.expressions.dsl.asNumber
import org.maplibre.compose.expressions.dsl.asString
import org.maplibre.compose.expressions.dsl.const
import org.maplibre.compose.expressions.dsl.feature
import org.maplibre.compose.expressions.dsl.not
import org.maplibre.compose.expressions.dsl.step
import org.maplibre.compose.interaction.ClickResult
import org.maplibre.compose.interaction.MapInteractions
import org.maplibre.compose.layers.CircleLayer
import org.maplibre.compose.layers.SymbolLayer
import org.maplibre.compose.map.MapState
import org.maplibre.compose.map.MaplibreMap
import org.maplibre.compose.map.rememberMapState
import org.maplibre.compose.sources.GeoJsonData
import org.maplibre.compose.sources.GeoJsonOptions
import org.maplibre.compose.sources.GeoJsonSourceHandle
import org.maplibre.compose.sources.rememberGeoJsonSource
import org.maplibre.compose.style.BaseStyle
import org.maplibre.spatialk.geojson.Feature
import org.maplibre.spatialk.geojson.FeatureCollection
import org.maplibre.spatialk.geojson.Geometry
import org.maplibre.spatialk.geojson.Point
import org.maplibre.spatialk.geojson.Position
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
import com.trm.warsawtransportmap.core.model.CameraPosition as SavedCameraPosition
import org.maplibre.compose.camera.CameraPosition as MapCameraPosition

@Composable
fun MapScreen(viewModel: MapViewModel = koinViewModel(), onNavigateToLines: () -> Unit) {
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
    },
  ) { paddingValues ->
    MapContent(
      vehicles = vehicles,
      isLoadingVehicles = isLoadingVehicles,
      initialCameraPosition = initialCameraPosition,
      snackbarHostState = snackbarHostState,
      onCameraPositionChange = viewModel::onCameraPositionChange,
      paddingValues = paddingValues,
    )
  }
}

@Composable
private fun MapContent(
  vehicles: List<Vehicle>,
  isLoadingVehicles: Boolean,
  initialCameraPosition: SavedCameraPosition?,
  snackbarHostState: SnackbarHostState,
  onCameraPositionChange: (MapCameraPosition) -> Unit,
  paddingValues: PaddingValues,
) {
  val scope = rememberCoroutineScope()
  var clickedCluster by remember { mutableStateOf<Feature<Geometry, JsonObject?>?>(null) }
  val boundingBox = rememberMapVehiclesBoundingBox(vehicles = vehicles, percentageIncrease = 0.1)
  val state =
    rememberVehiclesMapState(
      vehicles = vehicles,
      onClusterClick = { clickedCluster = it },
      onVehicleClick = { vehicle ->
        scope.launch {
          val message = vehicle.toUpdateTimeMessage() ?: return@launch
          snackbarHostState.currentSnackbarData?.dismiss()
          snackbarHostState.showSnackbar(message)
        }
      },
    )

  LaunchedEffect(state.isCameraMoving) {
    if (!state.isCameraMoving) onCameraPositionChange(state.cameraPosition)
  }

  initialCameraPosition?.let {
    LaunchedEffect(Unit) {
      state.animateCamera(
        CameraUpdate(
          target = Position(latitude = it.latitude, longitude = it.longitude),
          zoom = it.zoom,
        )
      )
    }
  }
    ?: run {
      if (
        state.cameraMoveReason == CameraMoveReason.NONE ||
          state.cameraMoveReason == CameraMoveReason.PROGRAMMATIC
      ) {
        MapCameraAnimateToBoundingBoxEffect(boundingBox, state)
      }
    }

  LaunchedEffect(clickedCluster) {
    clickedCluster
      ?.let {
        val handle = state.style.sources.filterIsInstance<GeoJsonSourceHandle>().first()
        CameraUpdate(
          target = (it.geometry as Point).coordinates,
          zoom = handle.getClusterExpansionZoom(it),
        )
      }
      ?.let {
        state.animateCamera(it)
        clickedCluster = null
      }
  }

  Box(modifier = Modifier.fillMaxSize()) {
    MapCanvas(state = state, isLoadingVehicles = isLoadingVehicles, paddingValues = paddingValues)

    AnimatedVisibility(
      visible = vehicles.isNotEmpty(),
      modifier = Modifier.align(Alignment.BottomEnd).padding(end = 16.dp, bottom = 112.dp),
    ) {
      FloatingActionButton(
        containerColor = MaterialTheme.colorScheme.secondaryContainer,
        onClick = { boundingBox?.let { scope.launch { state.animateCameraToBounds(it) } } },
      ) {
        Icon(
          imageVector = Icons.Default.FilterCenterFocus,
          contentDescription = stringResource(Res.string.center_map_content_description),
        )
      }
    }
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

@Composable
private fun rememberVehiclesMapState(
  vehicles: List<Vehicle>,
  onClusterClick: (Feature<Geometry, JsonObject?>) -> Unit,
  onVehicleClick: (Vehicle) -> Unit,
): MapState =
  rememberMapState(
    initialCameraPosition =
      MapCameraPosition(
        target =
          Position(
            latitude = MapConstants.WARSAW_CENTER_LAT,
            longitude = MapConstants.WARSAW_CENTER_LON,
          ),
        zoom = MapConstants.DEFAULT_ZOOM,
      ),
    baseStyle =
      BaseStyle.Uri(
        Res.getUri(if (isSystemInDarkTheme()) "files/dark_style.json" else "files/light_style.json")
      ),
  ) {
    val markersSource =
      rememberGeoJsonSource(
        data =
          GeoJsonData.Features(
            FeatureCollection(
              vehicles.map { vehicle ->
                Feature(
                  id = JsonPrimitive(vehicle.vehicleNumber),
                  geometry = Point(Position(vehicle.longitude, vehicle.latitude)),
                  properties = vehicle,
                )
              }
            )
          ),
        options = GeoJsonOptions(cluster = true, clusterRadius = 50, clusterMaxZoom = 14),
      )

    CircleLayer(
      id = "clustered-markers",
      source = markersSource,
      filter = feature.has("point_count"),
      color =
        step(
          input = feature["point_count"].asNumber(),
          fallback = const(MaterialTheme.colorScheme.tertiaryContainer),
          50 to const(MaterialTheme.colorScheme.secondaryContainer),
          100 to const(MaterialTheme.colorScheme.primaryContainer),
        ),
      opacity = const(.9f),
      radius =
        step(
          input = feature["point_count"].asNumber(),
          fallback = const(24.dp),
          50 to const(32.dp),
          100 to const(40.dp),
        ),
      onClick = { features ->
        features.firstOrNull(markersSource::isCluster)?.let {
          onClusterClick(it)
          ClickResult.Consume
        } ?: ClickResult.Pass
      },
    )

    SymbolLayer(
      id = "clustered-markers-count",
      source = markersSource,
      filter = feature.has("point_count"),
      textField = feature["point_count_abbreviated"].asString(),
      textFont = const(listOf("Noto Sans Regular")),
      textColor =
        step(
          input = feature["point_count"].asNumber(),
          fallback = const(MaterialTheme.colorScheme.onTertiaryContainer),
          50 to const(MaterialTheme.colorScheme.onSecondaryContainer),
          100 to const(MaterialTheme.colorScheme.onPrimaryContainer),
        ),
      textAllowOverlap = const(true),
      iconAllowOverlap = const(true),
    )

    CircleLayer(
      id = "unclustered-markers",
      source = markersSource,
      filter = !feature.has("point_count"),
      color = const(MaterialTheme.colorScheme.surfaceContainerHighest),
      radius = const(16.dp),
      strokeColor = const(MaterialTheme.colorScheme.onSurfaceVariant),
      strokeWidth = const(1.dp),
      onClick = { features ->
        features.firstOrNull()?.properties?.let { properties ->
          runCatching { onVehicleClick(Json.decodeFromJsonElement<Vehicle>(properties)) }
        }
        ClickResult.Consume
      },
    )

    SymbolLayer(
      id = "unclustered-markers-numbers",
      source = markersSource,
      filter = feature.has("lineNumber"),
      textField = feature["lineNumber"].asString(),
      textFont = const(listOf("Noto Sans Regular")),
      textColor = const(MaterialTheme.colorScheme.onSurface),
      textSize = const(12.sp),
      textAllowOverlap = const(true),
      iconAllowOverlap = const(true),
    )
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
