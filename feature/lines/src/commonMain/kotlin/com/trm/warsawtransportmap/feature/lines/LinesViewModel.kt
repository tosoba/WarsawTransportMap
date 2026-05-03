package com.trm.warsawtransportmap.feature.lines

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.trm.warsawtransportmap.core.common.model.Loadable
import com.trm.warsawtransportmap.core.domain.PreferencesRepository
import com.trm.warsawtransportmap.core.domain.TransportRepository
import com.trm.warsawtransportmap.core.model.Line
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class LinesViewModel(
  private val transportRepository: TransportRepository,
  private val preferencesRepository: PreferencesRepository,
) : ViewModel() {
  var state by mutableStateOf<Loadable<LinesState>>(Loadable.Loading)
    private set

  private val selectedLinesChanges = MutableSharedFlow<Set<String>>()

  init {
    loadLines()

    selectedLinesChanges
      .mapLatest(preferencesRepository::saveSelectedLines)
      .launchIn(viewModelScope)
  }

  fun loadLines() {
    viewModelScope.launch {
      state = Loadable.Loading
      try {
        val lines = transportRepository.getLines()
        state =
          Loadable.Loaded(
            LinesState(
              lines = lines.grouped(),
              selectedLines =
                preferencesRepository.selectedLines.firstOrNull() ?: lines.map(Line::number).toSet(),
            )
          )
      } catch (ex: Exception) {
        ensureActive()
        state = Loadable.Error(ex)
      }
    }
  }

  fun toggleLine(number: String) {
    when (val state = state) {
      is Loadable.Loaded -> {
        state.data.toggleLine(number)
        saveSelectedLines(state.data.selectedLines)
      }
      else -> return
    }
  }

  fun toggleAll() {
    when (val state = state) {
      is Loadable.Loaded -> {
        state.data.toggleAll()
        saveSelectedLines(state.data.selectedLines)
      }
      else -> return
    }
  }

  private fun saveSelectedLines(selected: Set<String>) {
    viewModelScope.launch { selectedLinesChanges.emit(selected) }
  }

  private fun List<Line>.grouped(): Map<String, List<Line>> = groupBy { (number) ->
    val intValue = number.toIntOrNull()
    if (intValue != null) {
      val group = (intValue / 100) * 100
      if (group == 0) "1" else group.toString()
    } else {
      number.firstOrNull()?.uppercase().orEmpty()
    }
  }
}
