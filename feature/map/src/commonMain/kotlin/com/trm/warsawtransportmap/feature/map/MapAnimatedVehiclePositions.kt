package com.trm.warsawtransportmap.feature.map

import com.trm.warsawtransportmap.core.model.Vehicle
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.time.Clock

internal class MapAnimatedVehiclePositions(private val scope: CoroutineScope) {
  private data class VehicleAnimation(
    val vehicle: Vehicle,
    val fromLat: Double,
    val fromLon: Double,
    val toLat: Double,
    val toLon: Double,
    val startMs: Long,
  )

  private val animations = mutableMapOf<String, VehicleAnimation>()

  private val _displayed = MutableStateFlow<List<Vehicle>>(emptyList())
  val displayed: StateFlow<List<Vehicle>> = _displayed.asStateFlow()

  private var renderJob: Job? = null

  fun update(newVehicles: List<Vehicle>) {
    if (newVehicles.isEmpty()) {
      clear()
      return
    }

    val now = Clock.System.now().toEpochMilliseconds()
    val currentById = _displayed.value.associateBy(Vehicle::vehicleNumber)

    animations.clear()
    newVehicles.forEach { vehicle ->
      val current = currentById[vehicle.vehicleNumber]
      animations[vehicle.vehicleNumber] =
        VehicleAnimation(
          vehicle = vehicle,
          fromLat = current?.latitude ?: vehicle.latitude,
          fromLon = current?.longitude ?: vehicle.longitude,
          toLat = vehicle.latitude,
          toLon = vehicle.longitude,
          startMs = now,
        )
    }

    startRendering()
  }

  fun clear() {
    renderJob?.cancel()
    renderJob = null
    animations.clear()
    _displayed.value = emptyList()
  }

  private fun startRendering() {
    renderJob?.cancel()
    renderJob = scope.launch {
      while (isActive) {
        val now = Clock.System.now().toEpochMilliseconds()
        var allSettled = true

        _displayed.value =
          animations.values.map { anim ->
            val t = ((now - anim.startMs).toFloat() / ANIM_DURATION_MS).coerceIn(0f, 1f)
            if (t < 1f) allSettled = false
            val easedT = smoothStep(t).toDouble()
            anim.vehicle.copy(
              latitude = lerp(anim.fromLat, anim.toLat, easedT),
              longitude = lerp(anim.fromLon, anim.toLon, easedT),
            )
          }

        if (allSettled) break
        delay(FRAME_DELAY_MS)
      }
    }
  }

  private fun lerp(a: Double, b: Double, t: Double): Double = a + (b - a) * t

  private fun smoothStep(t: Float): Float = t * t * (3f - 2f * t)

  companion object {
    private const val ANIM_DURATION_MS = 1_000L

    private const val FRAME_DELAY_MS = 16L
  }
}
