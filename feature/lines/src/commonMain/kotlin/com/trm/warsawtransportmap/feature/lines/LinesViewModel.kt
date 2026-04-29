package com.trm.warsawtransportmap.feature.lines

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.trm.warsawtransportmap.core.common.model.Loadable
import com.trm.warsawtransportmap.core.domain.PreferencesRepository
import com.trm.warsawtransportmap.core.domain.TransportRepository
import com.trm.warsawtransportmap.core.model.Line
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class LinesViewModel(
  private val transportRepository: TransportRepository,
  private val preferencesRepository: PreferencesRepository,
) : ViewModel() {
  private val _state = MutableStateFlow<Loadable<Map<String, List<LineState>>>>(Loadable.Loading)
  val state: StateFlow<Loadable<Map<String, List<LineState>>>> = _state.asStateFlow()

  private val selectedLinesChanges = MutableSharedFlow<Set<String>>()

  init {
    loadLines()

    selectedLinesChanges
      .mapLatest(preferencesRepository::saveSelectedLines)
      .launchIn(viewModelScope)
  }

  fun loadLines() {
    viewModelScope.launch {
      _state.value = Loadable.Loading

      try {
        val lines = transportRepository.getLines()
        val savedSelected = preferencesRepository.selectedLines.firstOrNull()
        val initialSelected = savedSelected ?: lines.map(Line::number).toSet()
        _state.value =
          Loadable.Loaded(
            lines
              .map {
                LineState(number = it.number, isSelected = initialSelected.contains(it.number))
              }
              .grouped()
          )
        if (savedSelected == null) {
          saveSelectedLines(initialSelected)
        }
      } catch (ex: Exception) {
        ensureActive()
        _state.value = Loadable.Error(ex.message)
      }
    }
  }

  fun toggleLine(number: String) {
    _state.update { currentState ->
      if (currentState !is Loadable.Loaded) return@update currentState

      val newGroups =
        currentState.data.mapValues { (_, lines) ->
          lines.map { if (it.number == number) it.copy(isSelected = !it.isSelected) else it }
        }
      saveSelectedLines(
        newGroups.values.flatten().filter(LineState::isSelected).map(LineState::number).toSet()
      )
      Loadable.Loaded(newGroups)
    }
  }

  fun toggleAll() {
    _state.update { currentState ->
      if (currentState !is Loadable.Loaded) return@update currentState

      val allLines = currentState.data.values.flatten()
      val allSelected = allLines.isNotEmpty() && allLines.all(LineState::isSelected)
      val toggled = !allSelected

      val newGroups =
        currentState.data.mapValues { (_, lines) -> lines.map { it.copy(isSelected = toggled) } }

      saveSelectedLines(
        if (toggled) newGroups.values.flatten().map(LineState::number).toSet() else emptySet()
      )
      Loadable.Loaded(newGroups)
    }
  }

  private fun saveSelectedLines(selected: Set<String>) {
    viewModelScope.launch { selectedLinesChanges.emit(selected) }
  }

  private fun List<LineState>.grouped(): Map<String, List<LineState>> = groupBy { line ->
    val number = line.number
    val intValue = number.toIntOrNull()
    if (intValue != null) {
      val group = (intValue / 100) * 100
      if (group == 0) "1" else group.toString()
    } else {
      number.firstOrNull()?.uppercase().orEmpty()
    }
  }
}
