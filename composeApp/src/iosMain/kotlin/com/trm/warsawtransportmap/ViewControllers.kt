package com.trm.warsawtransportmap

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeUIViewController
import com.trm.warsawtransportmap.feature.lines.LinesScreen
import com.trm.warsawtransportmap.feature.lines.LinesScreenStateBridge
import com.trm.warsawtransportmap.feature.map.MapScreen
import org.koin.core.context.startKoin

fun initKoin() {
  startKoin { modules(appModules) }
}

@OptIn(ExperimentalComposeUiApi::class)
fun mapViewController(onVehicleCountChanged: (Int) -> Unit) =
  ComposeUIViewController(configure = { opaque = false }) {
    AppTheme { MapScreen(onVehicleCountChanged) }
  }

@OptIn(ExperimentalComposeUiApi::class)
fun linesViewController(
  stateBridge: LinesScreenStateBridge,
  onSelectionStateChanged: (Boolean?) -> Unit,
) =
  ComposeUIViewController(configure = { opaque = false }) {
    AppTheme {
      LinesScreen(
        stateBridge = stateBridge,
        onSelectionStateChanged = onSelectionStateChanged,
      )
    }
  }
