package com.trm.warsawtransportmap.feature.lines

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.skydoves.flow.operators.restartable.RestartableStateFlow
import com.skydoves.flow.operators.restartable.restartableStateIn
import com.trm.warsawtransportmap.core.common.model.Loadable
import com.trm.warsawtransportmap.core.domain.PreferencesRepository
import com.trm.warsawtransportmap.core.domain.TransportRepository
import com.trm.warsawtransportmap.core.model.Line
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch

class LinesViewModel(
  private val transportRepository: TransportRepository,
  private val preferencesRepository: PreferencesRepository,
) : ViewModel() {
  val state: RestartableStateFlow<Loadable<LinesState>> = flow {
    emit(Loadable.Loading)

    val lines = transportRepository.getLines()
    val numbers = lines.map(Line::number).toSet()
    emit(
      Loadable.Loaded(
        LinesState(
          lines = lines.grouped(),
          selectedLines =
            preferencesRepository.selectedLines.firstOrNull()?.filter { it in numbers }?.toSet()
              ?: numbers,
        )
      )
    )
  }
    .catch { emit(Loadable.Error(it)) }
    .restartableStateIn(
      scope = viewModelScope,
      started = SharingStarted.Lazily,
      initialValue = Loadable.Loading,
    )

  private var saveSelectedLinesJob: Job? = null

  fun toggleLine(number: String) {
    when (val state = state.value) {
      is Loadable.Loaded -> {
        state.data.toggleLine(number)
        saveSelectedLines(state.data.selectedLines)
      }
      else -> {
        return
      }
    }
  }

  fun toggleAll() {
    when (val state = state.value) {
      is Loadable.Loaded -> {
        state.data.toggleAll()
        saveSelectedLines(state.data.selectedLines)
      }
      else -> {
        return
      }
    }
  }

  private fun saveSelectedLines(selected: Set<String>) {
    saveSelectedLinesJob?.cancel()
    saveSelectedLinesJob = viewModelScope.launch {
      preferencesRepository.saveSelectedLines(selected)
    }
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
