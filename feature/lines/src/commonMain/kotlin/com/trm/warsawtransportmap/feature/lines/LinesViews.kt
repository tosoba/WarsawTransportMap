package com.trm.warsawtransportmap.feature.lines

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
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
import androidx.compose.material3.AppBarWithSearch
import androidx.compose.material3.Button
import androidx.compose.material3.CircularWavyProgressIndicator
import androidx.compose.material3.ExpandedFullScreenContainedSearchBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.SearchBarValue
import androidx.compose.material3.Text
import androidx.compose.material3.ToggleButton
import androidx.compose.material3.rememberSearchBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.trm.warsawtransportmap.core.common.extensions.toErrorStringResource
import com.trm.warsawtransportmap.core.common.model.Loadable
import com.trm.warsawtransportmap.core.model.Line
import dev.icerock.moko.resources.compose.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun LinesTopBar(onBackClick: () -> Unit) {
  val textFieldState = rememberTextFieldState()
  val searchBarState = rememberSearchBarState(initialValue = SearchBarValue.Expanded)
  val viewModel = koinViewModel<LinesViewModel>()
  val state by viewModel.state.collectAsStateWithLifecycle()

  LaunchedEffect(searchBarState.targetValue) {
    if (searchBarState.targetValue == SearchBarValue.Collapsed) onBackClick()
  }

  val inputField =
    @Composable {
      SearchBarDefaults.InputField(
        textFieldState = textFieldState,
        searchBarState = searchBarState,
        onSearch = {},
        readOnly = state is Loadable.Loading,
        leadingIcon = {
          IconButton(onClick = onBackClick) {
            Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
          }
        },
        placeholder = {
          Text(text = stringResource(FeatureLinesMR.strings.search_lines_placeholder))
        },
        trailingIcon = {
          Row(verticalAlignment = Alignment.CenterVertically) {
            LinesClearSearchTextButton(textFieldState)

            LinesToggleAllButton(
              isVisible = state is Loadable.Loaded,
              allSelected = (state as? Loadable.Loaded)?.data?.allSelected ?: false,
              onToggleAll = viewModel::toggleAll,
            )
          }
        },
        modifier = Modifier.fillMaxWidth(),
      )
    }

  @OptIn(ExperimentalMaterial3Api::class)
  AppBarWithSearch(
    state = searchBarState,
    inputField = inputField,
  )

  ExpandedFullScreenContainedSearchBar(
    state = searchBarState,
    inputField = inputField,
  ) {
    LinesContent(
      state = state,
      searchText = textFieldState.text.toString(),
      modifier = Modifier.fillMaxSize(),
      onRetryClick = viewModel.state::restart,
      onLineClick = viewModel::toggleLine,
    )
  }
}

@Composable
private fun RowScope.LinesClearSearchTextButton(textFieldState: TextFieldState) {
  AnimatedVisibility(visible = textFieldState.text.isNotEmpty()) {
    IconButton(onClick = textFieldState::clearText) {
      Icon(
        imageVector = Icons.Default.Close,
        contentDescription = stringResource(FeatureLinesMR.strings.clear_content_description),
      )
    }
  }
}

@Composable
private fun RowScope.LinesToggleAllButton(
  isVisible: Boolean,
  allSelected: Boolean,
  onToggleAll: () -> Unit,
) {
  AnimatedVisibility(visible = isVisible, enter = fadeIn(), exit = fadeOut()) {
    IconButton(onClick = onToggleAll) {
      Icon(
        imageVector = if (allSelected) Icons.Default.Deselect else Icons.Default.SelectAll,
        contentDescription =
          stringResource(
            if (allSelected) FeatureLinesMR.strings.deselect_all_content_description
            else FeatureLinesMR.strings.select_all_content_description
          ),
      )
    }
  }
}

@Composable
internal fun LinesContent(
  state: Loadable<LinesState>,
  searchText: String,
  modifier: Modifier = Modifier,
  paddingValues: PaddingValues = PaddingValues.Zero,
  onRetryClick: () -> Unit,
  onLineClick: (String) -> Unit,
) {
  Crossfade(targetState = state, modifier = modifier) { loadableState ->
    when (loadableState) {
      Loadable.Loading -> {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
          CircularWavyProgressIndicator()
        }
      }
      is Loadable.Loaded -> {
        LinesGrid(
          lines =
            remember(searchText, loadableState) {
              if (searchText.isBlank()) {
                loadableState.data.lines
              } else {
                loadableState.data.lines
                  .mapValues { (_, lines) ->
                    lines.filter { it.number.contains(searchText, ignoreCase = true) }
                  }
                  .filterValues { it.isNotEmpty() }
              }
            },
          selectedLines = loadableState.data.selectedLines,
          paddingValues = paddingValues,
          onLineClick = onLineClick,
        )
      }
      is Loadable.Error -> {
        Box(modifier = Modifier.fillMaxSize().padding(16.dp), contentAlignment = Alignment.Center) {
          Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
              text = loadableState.throwable.toErrorStringResource(),
              color = MaterialTheme.colorScheme.onBackground,
            )

            Button(onClick = onRetryClick, modifier = Modifier.padding(top = 16.dp)) {
              Text(text = stringResource(FeatureLinesMR.strings.retry_button))
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
  paddingValues: PaddingValues,
  onLineClick: (String) -> Unit,
) {
  LazyVerticalGrid(
    columns = GridCells.Adaptive(minSize = 80.dp),
    contentPadding = PaddingValues(16.dp),
    horizontalArrangement = Arrangement.spacedBy(16.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp),
    modifier = Modifier.fillMaxSize(),
  ) {
    paddingValues
      .calculateTopPadding()
      .takeIf { it > 0.dp }
      ?.let {
        item(span = { GridItemSpan(maxLineSpan) }, key = "top-padding") {
          Spacer(modifier = Modifier.height(it))
        }
      }

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

    if (lines.isEmpty()) {
      item(span = { GridItemSpan(maxLineSpan) }, key = "empty-lines") {
        LinesEmptyText()
      }
    }

    paddingValues
      .calculateBottomPadding()
      .takeIf { it > 0.dp }
      ?.let {
        item(span = { GridItemSpan(maxLineSpan) }, key = "bottom-padding") {
          Spacer(modifier = Modifier.height(it))
        }
      }
  }
}

@Composable
private fun LazyGridItemScope.LinesEmptyText() {
  Text(
    text = stringResource(FeatureLinesMR.strings.no_lines_match_filter),
    style = MaterialTheme.typography.headlineSmallEmphasized,
    color = MaterialTheme.colorScheme.onBackground,
    textAlign = TextAlign.Center,
    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp).animateItem(),
  )
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
