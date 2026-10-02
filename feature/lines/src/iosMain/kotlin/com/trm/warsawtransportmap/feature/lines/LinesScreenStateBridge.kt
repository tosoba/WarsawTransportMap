package com.trm.warsawtransportmap.feature.lines

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

class LinesScreenStateBridge {
  var searchText by mutableStateOf("")

  var toggleAllAction: (() -> Unit)? = null

  fun toggleAllLines() {
    toggleAllAction?.invoke()
  }
}
