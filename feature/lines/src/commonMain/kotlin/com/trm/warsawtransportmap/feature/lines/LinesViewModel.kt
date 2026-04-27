package com.trm.warsawtransportmap.feature.lines

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.trm.warsawtransportmap.core.common.model.Loadable
import com.trm.warsawtransportmap.core.domain.TransportRepository
import com.trm.warsawtransportmap.core.model.Line
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class LinesViewModel(private val repository: TransportRepository) : ViewModel() {
  private val _state = MutableStateFlow<Loadable<List<Line>>>(Loadable.Loading)
  val state: StateFlow<Loadable<List<Line>>> = _state.asStateFlow()

  init {
    loadLines()
  }

  fun loadLines() {
    viewModelScope.launch {
      _state.value = Loadable.Loading
      try {
        _state.value = Loadable.Loaded(repository.getLines())
      } catch (ex: Exception) {
        ensureActive()
        _state.value = Loadable.Error(ex.message)
      }
    }
  }
}
