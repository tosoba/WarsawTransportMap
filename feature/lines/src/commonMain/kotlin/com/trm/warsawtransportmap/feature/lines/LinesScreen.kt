package com.trm.warsawtransportmap.feature.lines

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.ToggleButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateSetOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.trm.warsawtransportmap.core.common.model.Loadable
import com.trm.warsawtransportmap.core.model.Line
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun LinesScreen(viewModel: LinesViewModel = koinViewModel()) {
  Scaffold {
    Crossfade(
      targetState = viewModel.state.collectAsStateWithLifecycle().value,
      modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).padding(it),
    ) { state ->
      when (state) {
        Loadable.Loading -> {
          Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
          }
        }
        is Loadable.Loaded -> {
          LinesGrid(lines = state.data)
        }
        is Loadable.Error -> {
          Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
              Text(
                text = state.message ?: "Unknown error",
                color = MaterialTheme.colorScheme.onBackground,
              )

              Button(onClick = viewModel::loadLines, modifier = Modifier.padding(top = 16.dp)) {
                Text("Retry")
              }
            }
          }
        }
      }
    }
  }
}

@Composable
private fun LinesGrid(lines: List<Line>) {
  val selectedLines = rememberSaveable { mutableStateSetOf<String>() }
  LazyVerticalGrid(
    columns = GridCells.Adaptive(minSize = 80.dp),
    contentPadding = PaddingValues(16.dp),
    horizontalArrangement = Arrangement.spacedBy(16.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp),
    modifier = Modifier.fillMaxSize(),
  ) {
    items(lines, key = Line::number) { line ->
      val isSelected = selectedLines.contains(line.number)
      LineButton(
        line = line,
        isSelected = isSelected,
        onClick = {
          if (isSelected) selectedLines.remove(line.number) else selectedLines.add(line.number)
        },
      )
    }
  }
}

@Composable
private fun LineButton(line: Line, isSelected: Boolean, onClick: () -> Unit) {
  @OptIn(ExperimentalMaterial3ExpressiveApi::class)
  ToggleButton(checked = isSelected, onCheckedChange = { onClick() }) {
    Box(contentAlignment = Alignment.Center) {
      Text(
        text = line.number,
        style = MaterialTheme.typography.titleMedium,
        maxLines = 1,
        modifier = Modifier.basicMarquee(iterations = Int.MAX_VALUE),
      )
    }
  }
}
