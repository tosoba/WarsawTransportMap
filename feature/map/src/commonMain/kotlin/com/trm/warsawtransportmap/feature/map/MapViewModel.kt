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
import kotlin.coroutines.cancellation.CancellationException
import kotlin.time.Clock
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class MapViewModel(
  private val transportRepository: TransportRepository,
  private val preferencesRepository: PreferencesRepository,
  private val lifecycle: Lifecycle,
  private val savedStateHandle: SavedStateHandle,
) : ViewModel() {
  val initialCameraPosition: StateFlow<CameraPosition?> =
    preferencesRepository.cameraPosition.stateIn(
      scope = viewModelScope,
      started = SharingStarted.Eagerly,
      initialValue = null,
    )

  private val cameraPositionChanges = MutableSharedFlow<MapCameraPosition>()

  private val selectedLines: StateFlow<Set<String>?> =
    preferencesRepository.selectedLines.stateIn(
      scope = viewModelScope,
      started = SharingStarted.Eagerly,
      initialValue = null,
    )

  private val _allVehicles = MutableStateFlow<List<Vehicle>>(emptyList())
  private val _vehicles = MutableStateFlow<List<Vehicle>>(emptyList())
  val vehicles: StateFlow<List<Vehicle>> = _vehicles.asStateFlow()

  private val _isLoadingVehicles = MutableStateFlow(false)
  val isLoadingVehicles: StateFlow<Boolean> = _isLoadingVehicles.asStateFlow()

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
      Lifecycle.Event.ON_START -> {
        val lines = selectedLines.value
        if (lines == null || lines.isNotEmpty()) {
          startPeriodicFetch()
        }
      }
      Lifecycle.Event.ON_STOP -> {
        lastBackgroundTimeEpoch = Clock.System.now().toEpochMilliseconds()
        stopPeriodicFetch()
      }
      else -> {}
    }
  }

  init {
    lifecycle.addObserver(lifecycleObserver)

    selectedLines
      .onEach { lines ->
        if (lines != null && lines.isEmpty()) {
          stopPeriodicFetch()
          _allVehicles.value = emptyList()
          _vehicles.value = emptyList()
          wasExecuted = false
        } else {
          _vehicles.value = filterVehicles(_allVehicles.value, lines)
          if (lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)) {
            startPeriodicFetch()
          }
        }
      }
      .launchIn(viewModelScope)

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
    if (fetchJob?.isActive == true) return

    val backgroundTime = lastBackgroundTimeEpoch
    lastBackgroundTimeEpoch = 0L

    fetchJob = viewModelScope.launch {
      delay(
        when {
          !wasExecuted -> 0L
          backgroundTime != 0L ->
            maxOf(
              0L,
              MAX_FETCH_DELAY_MILLIS - (Clock.System.now().toEpochMilliseconds() - backgroundTime),
            )
          else -> MAX_FETCH_DELAY_MILLIS
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
    val coroutineContext = currentCoroutineContext()
    _isLoadingVehicles.value = true
    try {
      val vehicles =
        transportRepository.getVehicles().filter { vehicle ->
          calculateDistanceBetweenKm(
            lat1 = WARSAW_CENTER_LAT,
            lon1 = WARSAW_CENTER_LON,
            lat2 = vehicle.latitude,
            lon2 = vehicle.longitude,
          ) <= MAX_DISTANCE_KM
        }
      _allVehicles.value = vehicles
      _vehicles.value = filterVehicles(vehicles, selectedLines.value)
    } catch (ex: Exception) {
      if (ex is CancellationException) throw ex
      _errors.send(ex)
    } finally {
      if (coroutineContext.isActive) wasExecuted = true
      _isLoadingVehicles.value = false
    }
  }

  private fun filterVehicles(vehicles: List<Vehicle>, lines: Set<String>?): List<Vehicle> =
    if (lines == null) vehicles else vehicles.filter { lines.contains(it.lineNumber) }

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
