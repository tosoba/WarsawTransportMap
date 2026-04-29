package com.trm.warsawtransportmap.feature.lines

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyGridItemScope
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.text.input.clearText
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Deselect
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SelectAll
import androidx.compose.material3.AppBarWithSearch
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.ToggleButton
import androidx.compose.material3.rememberContainedSearchBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import com.trm.warsawtransportmap.core.common.model.Loadable
import com.trm.warsawtransportmap.core.model.Line
import kotlinx.coroutines.launch
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun LinesScreen(viewModel: LinesViewModel = koinViewModel(), onBackClick: () -> Unit) {
  val state = viewModel.state
  val textFieldState = rememberTextFieldState()
  val searchBarState = rememberContainedSearchBarState()
  val scope = rememberCoroutineScope()
  val scrollBehavior = SearchBarDefaults.enterAlwaysSearchBarScrollBehavior()
  val appBarWithSearchColors =
    SearchBarDefaults.appBarWithSearchColors(
      searchBarColors = SearchBarDefaults.containedColors(state = searchBarState)
    )

  Scaffold(
    modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
    topBar = {
      AppBarWithSearch(
        scrollBehavior = scrollBehavior,
        state = searchBarState,
        colors = appBarWithSearchColors,
        inputField = {
          SearchBarDefaults.InputField(
            textFieldState = textFieldState,
            searchBarState = searchBarState,
            colors = appBarWithSearchColors.searchBarColors.inputFieldColors,
            enabled = state is Loadable.Loaded,
            onSearch = { scope.launch { searchBarState.animateToCollapsed() } },
            placeholder = { Text(text = "Search lines") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            trailingIcon = {
              AnimatedVisibility(textFieldState.text.isNotEmpty()) {
                IconButton(onClick = { textFieldState.clearText() }) {
                  Icon(Icons.Default.Close, contentDescription = "Clear")
                }
              }
            },
            modifier = Modifier.fillMaxWidth(),
          )
        },
        navigationIcon = {
          IconButton(onClick = onBackClick) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
          }
        },
        actions = {
          val allSelected =
            remember(state) { (state as? Loadable.Loaded)?.data?.allSelected ?: false }
          IconButton(enabled = state is Loadable.Loaded, onClick = viewModel::toggleAll) {
            Icon(
              imageVector = if (allSelected) Icons.Default.Deselect else Icons.Default.SelectAll,
              contentDescription = if (allSelected) "Deselect all" else "Select all",
            )
          }
        },
      )
    },
  ) { padding ->
    Crossfade(
      targetState = state,
      modifier =
        Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).padding(padding),
    ) { loadableState ->
      when (loadableState) {
        Loadable.Loading -> {
          Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
          }
        }
        is Loadable.Loaded -> {
          LinesGrid(
            lines =
              remember(textFieldState.text, loadableState) {
                val query = textFieldState.text.toString()
                if (query.isBlank()) {
                  loadableState.data.lines
                } else {
                  loadableState.data.lines
                    .mapValues { (_, lines) ->
                      lines.filter { it.number.contains(query, ignoreCase = true) }
                    }
                    .filterValues { it.isNotEmpty() }
                }
              },
            selectedLines = loadableState.data.selectedLines,
            onLineClick = viewModel::toggleLine,
          )
        }
        is Loadable.Error -> {
          Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
              Text(
                text = loadableState.message ?: "Unknown error",
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
private fun LinesGrid(
  lines: Map<String, List<Line>>,
  selectedLines: Set<String>,
  onLineClick: (String) -> Unit,
) {
  LazyVerticalGrid(
    columns = GridCells.Adaptive(minSize = 80.dp),
    contentPadding = PaddingValues(16.dp),
    horizontalArrangement = Arrangement.spacedBy(16.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp),
    modifier = Modifier.fillMaxSize(),
  ) {
    lines.forEach { (header, lines) ->
      item(span = { GridItemSpan(maxLineSpan) }, key = "header_$header") { LineGroupHeader(header) }

      items(lines, key = Line::number) { line ->
        LineButton(
          line = line,
          isSelected = selectedLines.contains(line.number),
          onClick = { onLineClick(line.number) },
        )
      }
    }
  }
}

@Composable
private fun LazyGridItemScope.LineGroupHeader(title: String) {
  Column(
    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp).animateItem()
  ) {
    Text(
      text = title,
      style = MaterialTheme.typography.headlineSmallEmphasized,
      color = MaterialTheme.colorScheme.onBackground,
    )

    Spacer(modifier = Modifier.height(4.dp))

    HorizontalDivider()
  }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun LazyGridItemScope.LineButton(line: Line, isSelected: Boolean, onClick: () -> Unit) {
  ToggleButton(
    checked = isSelected,
    onCheckedChange = { onClick() },
    modifier = Modifier.animateItem(),
  ) {
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
