package com.trm.warsawtransportmap.feature.map

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.trm.warsawtransportmap.core.common.extensions.calculateDistanceBetweenKm
import com.trm.warsawtransportmap.core.domain.PreferencesRepository
import com.trm.warsawtransportmap.core.domain.TransportRepository
import com.trm.warsawtransportmap.core.model.CameraPosition
import com.trm.warsawtransportmap.core.model.Vehicle
import com.trm.warsawtransportmap.feature.map.MapConstants.WARSAW_CENTER_LAT
import com.trm.warsawtransportmap.feature.map.MapConstants.WARSAW_CENTER_LON
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.coroutines.cancellation.CancellationException
import kotlin.time.Clock

@OptIn(ExperimentalCoroutinesApi::class)
class MapViewModel(
  private val transportRepository: TransportRepository,
  private val preferencesRepository: PreferencesRepository,
  private val lifecycle: Lifecycle,
  private val savedStateHandle: SavedStateHandle,
) : ViewModel() {
  val initialCameraPosition =
    flow { emit(preferencesRepository.cameraPosition.firstOrNull()) }
      .stateIn(scope = viewModelScope, started = SharingStarted.Eagerly, initialValue = null)

  private val cameraPositionChanges = MutableSharedFlow<MapCameraPosition>()

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

    cameraPositionChanges
      .mapLatest {
        preferencesRepository.saveCameraPosition(
          CameraPosition(
            latitude = it.target.latitude,
            longitude = it.target.longitude,
            zoom = it.zoom,
          )
        )
      }
      .launchIn(viewModelScope)
  }

  fun onCameraPositionChange(position: MapCameraPosition) {
    viewModelScope.launch { cameraPositionChanges.emit(position) }
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
        transportRepository.getVehicles().filter { vehicle ->
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

    private const val MAX_DISTANCE_KM = 50.0
  }
}
