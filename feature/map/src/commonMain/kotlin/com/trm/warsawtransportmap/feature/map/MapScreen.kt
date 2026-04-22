package com.trm.warsawtransportmap.feature.map

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.serialization.json.JsonPrimitive
import org.koin.compose.viewmodel.koinViewModel
import org.maplibre.compose.camera.rememberCameraState
import org.maplibre.compose.expressions.dsl.asNumber
import org.maplibre.compose.expressions.dsl.asString
import org.maplibre.compose.expressions.dsl.const
import org.maplibre.compose.expressions.dsl.feature
import org.maplibre.compose.expressions.dsl.not
import org.maplibre.compose.expressions.dsl.step
import org.maplibre.compose.layers.CircleLayer
import org.maplibre.compose.layers.SymbolLayer
import org.maplibre.compose.map.GestureOptions
import org.maplibre.compose.map.MapOptions
import org.maplibre.compose.map.MaplibreMap
import org.maplibre.compose.map.OrnamentOptions
import org.maplibre.compose.sources.GeoJsonData
import org.maplibre.compose.sources.GeoJsonOptions
import org.maplibre.compose.sources.rememberGeoJsonSource
import org.maplibre.compose.style.BaseStyle
import org.maplibre.compose.util.ClickResult
import org.maplibre.spatialk.geojson.Feature
import org.maplibre.spatialk.geojson.FeatureCollection
import org.maplibre.spatialk.geojson.Point
import org.maplibre.spatialk.geojson.Position
import warsawtransportmap.feature.map.generated.resources.Res // Import generated Res

@Composable
fun MapScreen(viewModel: MapViewModel = koinViewModel()) {
  val vehicles by viewModel.vehicles.collectAsStateWithLifecycle()
  val cameraState = rememberCameraState()

  MaplibreMap(
    modifier = Modifier.fillMaxSize(),
    baseStyle =
      BaseStyle.Uri(
        Res.getUri(if (isSystemInDarkTheme()) "files/dark_style.json" else "files/light_style.json")
      ),
    options =
      MapOptions(
        gestureOptions = GestureOptions.RotationLocked,
        ornamentOptions = OrnamentOptions.AllDisabled,
      ),
    cameraState = cameraState,
  ) {
    if (vehicles.isEmpty()) return@MaplibreMap

    val geoJsonData =
      GeoJsonData.Features(
        FeatureCollection(
          vehicles.map { vehicle ->
            Feature(
              id = JsonPrimitive(vehicle.vehicleNumber),
              geometry = Point(Position(vehicle.longitude, vehicle.latitude)),
              properties = Unit,
            )
          }
        )
      )

    val markersSource =
      rememberGeoJsonSource(
        data = geoJsonData,
        options = GeoJsonOptions(cluster = true, clusterRadius = 50, clusterMaxZoom = 14),
      )

    CircleLayer(
      id = "clustered-markers",
      source = markersSource,
      filter = feature.has("point_count"),
      color = const(Color(0xFF51bbd6)),
      opacity = const(0.6f),
      radius =
        step(
          input = feature["point_count"].asNumber(),
          fallback = const(20.dp),
          10 to const(30.dp),
          50 to const(40.dp),
        ),
      onClick = { features -> ClickResult.Consume },
    )

    SymbolLayer(
      id = "clustered-markers-count",
      source = markersSource,
      filter = feature.has("point_count"),
      textField = feature["point_count"].asString(),
      textColor = const(Color.White),
    )

    CircleLayer(
      id = "unclustered-markers",
      source = markersSource,
      filter = !feature.has("point_count"),
      color = const(Color.Red),
      radius = const(10.dp),
      strokeColor = const(Color.White),
      strokeWidth = const(2.dp),
    )
  }
}
