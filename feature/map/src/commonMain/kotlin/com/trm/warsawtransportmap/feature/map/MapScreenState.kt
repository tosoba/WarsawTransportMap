package com.trm.warsawtransportmap.feature.map

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.trm.warsawtransportmap.core.common.extensions.MapCameraAnimateToBoundingBoxEffect
import com.trm.warsawtransportmap.core.common.extensions.rememberMapVehiclesBoundingBox
import com.trm.warsawtransportmap.core.model.Vehicle
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.decodeFromJsonElement
import org.maplibre.compose.camera.CameraMoveReason
import org.maplibre.compose.camera.CameraPosition
import org.maplibre.compose.camera.CameraUpdate
import org.maplibre.compose.expressions.dsl.asNumber
import org.maplibre.compose.expressions.dsl.asString
import org.maplibre.compose.expressions.dsl.const
import org.maplibre.compose.expressions.dsl.feature
import org.maplibre.compose.expressions.dsl.not
import org.maplibre.compose.expressions.dsl.step
import org.maplibre.compose.interaction.ClickResult
import org.maplibre.compose.layers.CircleLayer
import org.maplibre.compose.layers.SymbolLayer
import org.maplibre.compose.map.MapState
import org.maplibre.compose.map.rememberMapState
import org.maplibre.compose.sources.GeoJsonData
import org.maplibre.compose.sources.GeoJsonOptions
import org.maplibre.compose.sources.GeoJsonSourceHandle
import org.maplibre.compose.sources.rememberGeoJsonSource
import org.maplibre.compose.style.BaseStyle
import org.maplibre.spatialk.geojson.BoundingBox
import org.maplibre.spatialk.geojson.Feature
import org.maplibre.spatialk.geojson.FeatureCollection
import org.maplibre.spatialk.geojson.Geometry
import org.maplibre.spatialk.geojson.Point
import org.maplibre.spatialk.geojson.Position
import warsawtransportmap.feature.map.generated.resources.Res
import com.trm.warsawtransportmap.core.model.CameraPosition as SavedCameraPosition

internal data class MapScreenState(
  val mapState: MapState,
  private val boundingBox: BoundingBox?,
) {
  suspend fun animateToVehiclesBoundingBox() {
    boundingBox?.let { mapState.animateCameraToBounds(it) }
  }
}

@Composable
internal fun rememberMapScreenState(
  vehicles: List<Vehicle>,
  initialCameraPosition: SavedCameraPosition?,
  onCameraPositionChange: (CameraPosition) -> Unit,
  onVehicleClick: (Vehicle) -> Unit,
): MapScreenState {
  var clickedCluster by remember { mutableStateOf<Feature<Geometry, JsonObject?>?>(null) }
  val boundingBox = rememberMapVehiclesBoundingBox(vehicles = vehicles, percentageIncrease = 0.1)
  val mapState =
    rememberVehiclesMapState(
      vehicles = vehicles,
      onClusterClick = { clickedCluster = it },
      onVehicleClick = onVehicleClick,
    )

  LaunchedEffect(mapState.isCameraMoving) {
    if (!mapState.isCameraMoving) onCameraPositionChange(mapState.cameraPosition)
  }

  initialCameraPosition?.let {
    LaunchedEffect(Unit) {
      mapState.animateCamera(
        CameraUpdate(
          target = Position(latitude = it.latitude, longitude = it.longitude),
          zoom = it.zoom,
        )
      )
    }
  }
    ?: run {
      if (
        mapState.cameraMoveReason == CameraMoveReason.NONE ||
          mapState.cameraMoveReason == CameraMoveReason.PROGRAMMATIC
      ) {
        MapCameraAnimateToBoundingBoxEffect(boundingBox, mapState)
      }
    }

  LaunchedEffect(clickedCluster) {
    clickedCluster
      ?.let {
        val handle = mapState.style.sources.filterIsInstance<GeoJsonSourceHandle>().first()
        CameraUpdate(
          target = (it.geometry as Point).coordinates,
          zoom = handle.getClusterExpansionZoom(it),
        )
      }
      ?.let {
        mapState.animateCamera(it)
        clickedCluster = null
      }
  }

  return MapScreenState(mapState, boundingBox)
}

@Composable
private fun rememberVehiclesMapState(
  vehicles: List<Vehicle>,
  onClusterClick: (Feature<Geometry, JsonObject?>) -> Unit,
  onVehicleClick: (Vehicle) -> Unit,
): MapState =
  rememberMapState(
    initialCameraPosition =
      CameraPosition(
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
