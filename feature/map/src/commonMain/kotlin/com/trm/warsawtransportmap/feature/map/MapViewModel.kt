package com.trm.warsawtransportmap.feature.map

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.trm.warsawtransportmap.core.data.TransportRepository
import com.trm.warsawtransportmap.core.model.Vehicle
import kotlin.time.Clock
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

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
  private var lastRequestTimeEpoch: Long
    get() = savedStateHandle[KEY_LAST_REQUEST_TIME_EPOCH] ?: 0L
    set(value) {
      savedStateHandle[KEY_LAST_REQUEST_TIME_EPOCH] = value
    }

  private val lifecycleObserver = LifecycleEventObserver { _, event ->
    when (event) {
      Lifecycle.Event.ON_START -> handleForeground()
      Lifecycle.Event.ON_STOP -> handleBackground()
      else -> {}
    }
  }

  init {
    lifecycle.addObserver(lifecycleObserver)
  }

  private fun handleForeground() {
    val now = Clock.System.now().toEpochMilliseconds()
    if (lastRequestTimeEpoch == 0L || (now - lastRequestTimeEpoch) >= 30_000L) {
      fetchVehicles()
    }
    startPeriodicFetch()
  }

  private fun handleBackground() {
    stopPeriodicFetch()
  }

  private fun startPeriodicFetch() {
    stopPeriodicFetch()
    fetchJob = viewModelScope.launch {
      while (isActive) {
        delay(30_000)
        fetchVehicles()
      }
    }
  }

  private fun stopPeriodicFetch() {
    fetchJob?.cancel()
    fetchJob = null
  }

  private fun fetchVehicles() {
    viewModelScope.launch {
      try {
        lastRequestTimeEpoch = Clock.System.now().toEpochMilliseconds()
        val result = repository.getVehicles()
        _vehicles.value = result
      } catch (e: Exception) {
        _errors.send(e)
      }
    }
  }

  override fun onCleared() {
    lifecycle.removeObserver(lifecycleObserver)
    stopPeriodicFetch()
    super.onCleared()
  }

  companion object {
    private const val KEY_LAST_REQUEST_TIME_EPOCH = "last_request_time_epoch"
  }
}
