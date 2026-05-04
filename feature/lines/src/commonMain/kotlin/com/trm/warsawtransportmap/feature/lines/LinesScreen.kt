package com.trm.warsawtransportmap.feature.lines

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
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
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.clearText
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Deselect
import androidx.compose.material.icons.filled.SelectAll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SearchBar
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.ToggleButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.trm.warsawtransportmap.core.common.extensions.toErrorMessage
import com.trm.warsawtransportmap.core.common.model.Loadable
import com.trm.warsawtransportmap.core.model.Line
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import warsawtransportmap.feature.lines.generated.resources.Res
import warsawtransportmap.feature.lines.generated.resources.back_content_description
import warsawtransportmap.feature.lines.generated.resources.clear_content_description
import warsawtransportmap.feature.lines.generated.resources.deselect_all_content_description
import warsawtransportmap.feature.lines.generated.resources.retry_button
import warsawtransportmap.feature.lines.generated.resources.search_lines_placeholder
import warsawtransportmap.feature.lines.generated.resources.select_all_content_description

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun LinesScreen(viewModel: LinesViewModel = koinViewModel(), onBackClick: () -> Unit) {
  val state = viewModel.state
  val textFieldState = rememberTextFieldState()
  var expanded by remember { mutableStateOf(false) }

  Scaffold(
    topBar = {
      SearchBar(
        inputField = {
          SearchBarDefaults.InputField(
            state = textFieldState,
            onSearch = { expanded = false },
            expanded = expanded,
            onExpandedChange = { expanded = it },
            enabled = state is Loadable.Loaded,
            placeholder = { Text(text = stringResource(Res.string.search_lines_placeholder)) },
            leadingIcon = {
              IconButton(onClick = { if (expanded) expanded = false else onBackClick() }) {
                Icon(
                  imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                  contentDescription = stringResource(Res.string.back_content_description),
                )
              }
            },
            trailingIcon = {
              Row(verticalAlignment = Alignment.CenterVertically) {
                AnimatedVisibility(visible = textFieldState.text.isNotEmpty()) {
                  IconButton(onClick = textFieldState::clearText) {
                    Icon(
                      imageVector = Icons.Default.Close,
                      contentDescription = stringResource(Res.string.clear_content_description),
                    )
                  }
                }
                val allSelected =
                  remember(state) { (state as? Loadable.Loaded)?.data?.allSelected ?: false }
                IconButton(enabled = state is Loadable.Loaded, onClick = viewModel::toggleAll) {
                  Icon(
                    imageVector =
                      if (allSelected) Icons.Default.Deselect else Icons.Default.SelectAll,
                    contentDescription =
                      stringResource(
                        if (allSelected) Res.string.deselect_all_content_description
                        else Res.string.select_all_content_description
                      ),
                  )
                }
              }
            },
          )
        },
        expanded = expanded,
        onExpandedChange = { expanded = it },
        modifier =
          Modifier.fillMaxWidth()
            .padding(
              horizontal = if (expanded) 0.dp else 16.dp,
              vertical = if (expanded) 0.dp else 8.dp,
            ),
      ) {
        LinesScreenContent(
          state = state,
          textFieldState = textFieldState,
          viewModel = viewModel,
          modifier = Modifier.fillMaxSize(),
        )
      }
    }
  ) { padding ->
    LinesScreenContent(
      state = state,
      textFieldState = textFieldState,
      viewModel = viewModel,
      modifier =
        Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).padding(padding),
    )
  }
}

@Composable
private fun LinesScreenContent(
  state: Loadable<LinesState>,
  textFieldState: TextFieldState,
  viewModel: LinesViewModel,
  modifier: Modifier = Modifier,
) {
  Crossfade(targetState = state, modifier = modifier) { loadableState ->
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
        val errorMessage by
          produceState<String?>(initialValue = null, key1 = loadableState) {
            value = loadableState.throwable.toErrorMessage()
          }
        Box(modifier = Modifier.fillMaxSize().padding(16.dp), contentAlignment = Alignment.Center) {
          Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = errorMessage.orEmpty(), color = MaterialTheme.colorScheme.onBackground)

            Button(onClick = viewModel::loadLines, modifier = Modifier.padding(top = 16.dp)) {
              Text(text = stringResource(Res.string.retry_button))
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
