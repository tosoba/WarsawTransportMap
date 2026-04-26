package com.trm.warsawtransportmap.feature.map

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.trm.warsawtransportmap.core.common.extensions.calculateDistanceBetweenKm
import com.trm.warsawtransportmap.core.data.TransportRepository
import com.trm.warsawtransportmap.core.model.Vehicle
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.coroutines.cancellation.CancellationException
import kotlin.time.Clock

class MapViewModel(
  private val repository: TransportRepository,
  private val lifecycle: Lifecycle,
  private val savedStateHandle: SavedStateHandle,
) : ViewModel() {
  private val _vehicles = MutableStateFlow<List<Vehicle>>(emptyList())
  val vehicles: StateFlow<List<Vehicle>> = _vehicles.asStateFlow()

  private val _errors = Channel<Throwable>(Channel.UNLIMITED)
  val errors = _errors.receiveAsFlow()

  private var fetchJob: Job? = null

  private var lastBackgroundTimeEpoch: Long
    get() = savedStateHandle[KEY_LAST_BACKGROUND_TIME_EPOCH] ?: 0L
    set(value) {
      savedStateHandle[KEY_LAST_BACKGROUND_TIME_EPOCH] = value
    }

  private var wasExecuted: Boolean
    get() = savedStateHandle[KEY_WAS_EXECUTED] ?: false
    set(value) {
      savedStateHandle[KEY_WAS_EXECUTED] = value
    }

  private val lifecycleObserver = LifecycleEventObserver { _, event ->
    when (event) {
      Lifecycle.Event.ON_START -> startPeriodicFetch()
      Lifecycle.Event.ON_STOP -> {
        lastBackgroundTimeEpoch = Clock.System.now().toEpochMilliseconds()
        stopPeriodicFetch()
      }
      else -> {}
    }
  }

  init {
    lifecycle.addObserver(lifecycleObserver)
  }

  private fun startPeriodicFetch() {
    val backgroundTime = lastBackgroundTimeEpoch
    lastBackgroundTimeEpoch = 0L

    fetchJob = viewModelScope.launch {
      delay(
        when {
          !wasExecuted -> {
            0L
          }
          backgroundTime != 0L -> {
            maxOf(
              0L,
              MAX_FETCH_DELAY_MILLIS - (Clock.System.now().toEpochMilliseconds() - backgroundTime),
            )
          }
          else -> {
            MAX_FETCH_DELAY_MILLIS
          }
        }
      )
      while (isActive) {
        fetchVehicles()
        delay(MAX_FETCH_DELAY_MILLIS)
      }
    }
  }

  private fun stopPeriodicFetch() {
    fetchJob?.cancel()
    fetchJob = null
  }

  private suspend fun fetchVehicles() {
    try {
      _vehicles.value =
        repository.getVehicles().filter { vehicle ->
          calculateDistanceBetweenKm(
            lat1 = WARSAW_CENTER_LAT,
            lon1 = WARSAW_CENTER_LON,
            lat2 = vehicle.latitude,
            lon2 = vehicle.longitude,
          ) <= MAX_DISTANCE_KM
        }
    } catch (ex: Exception) {
      if (ex is CancellationException) throw ex
      _errors.send(ex)
    } finally {
      wasExecuted = true
    }
  }

  override fun onCleared() {
    lifecycle.removeObserver(lifecycleObserver)
    stopPeriodicFetch()
    super.onCleared()
  }

  companion object {
    private const val MAX_FETCH_DELAY_MILLIS = 30_000L

    private const val KEY_WAS_EXECUTED = "KEY_WAS_EXECUTED"
    private const val KEY_LAST_BACKGROUND_TIME_EPOCH = "KEY_LAST_BACKGROUND_TIME_EPOCH"

    private const val WARSAW_CENTER_LAT = 52.2318
    private const val WARSAW_CENTER_LON = 21.0060

    private const val MAX_DISTANCE_KM = 50.0
  }
}
