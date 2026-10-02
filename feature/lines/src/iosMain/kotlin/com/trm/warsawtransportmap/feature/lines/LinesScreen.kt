package com.trm.warsawtransportmap.feature.lines

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.trm.warsawtransportmap.core.common.model.Loadable
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun LinesScreen(
  stateBridge: LinesScreenStateBridge,
  onSelectionStateChanged: (Boolean?) -> Unit,
) {
  val viewModel = koinViewModel<LinesViewModel>()
  val state by viewModel.state.collectAsStateWithLifecycle()
  val selectionState = (state as? Loadable.Loaded)?.data?.allSelected

  DisposableEffect(viewModel) {
    stateBridge.toggleAllAction = viewModel::toggleAll
    onDispose { stateBridge.toggleAllAction = null }
  }

  LaunchedEffect(selectionState) {
    onSelectionStateChanged(selectionState)
  }

  Scaffold {
    LinesContent(
      state = state,
      searchText = stateBridge.searchText,
      modifier = Modifier.fillMaxSize(),
      paddingValues = it,
      onRetryClick = viewModel.state::restart,
      onLineClick = viewModel::toggleLine,
    )
  }
}
