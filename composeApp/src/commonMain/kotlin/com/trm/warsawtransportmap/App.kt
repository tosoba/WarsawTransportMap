package com.trm.warsawtransportmap

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Map
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ShortNavigationBar
import androidx.compose.material3.ShortNavigationBarItem
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowHeightSizeClass
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.unit.DpSize
import com.trm.warsawtransportmap.feature.lines.LinesTopBar
import com.trm.warsawtransportmap.feature.map.MapCenterFab
import com.trm.warsawtransportmap.feature.map.MapScreenContent
import com.trm.warsawtransportmap.feature.map.MapTopBar
import com.trm.warsawtransportmap.feature.map.rememberMapScreenState
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.KoinApplication
import org.koin.dsl.KoinConfiguration
import warsawtransportmap.composeapp.generated.resources.Res
import warsawtransportmap.composeapp.generated.resources.lines_navigation_label
import warsawtransportmap.composeapp.generated.resources.map_navigation_label

@Composable
fun App() {
  KoinApplication(configuration = KoinConfiguration { modules(appModules) }) {
    AppTheme {
      val snackbarHostState = remember(::SnackbarHostState)
      var selectedPage by remember { mutableStateOf(AppPage.MAP) }
      val useNavigationRail = shouldUseNavigationRail()
      val onPageSelected: (AppPage) -> Unit = { selectedPage = it }

      val mapScreenState = rememberMapScreenState(snackbarHostState)

      Row(modifier = Modifier.fillMaxSize()) {
        if (useNavigationRail) {
          AppNavigationRail(currentPage = selectedPage, onPageSelected = onPageSelected)
        }

        Scaffold(
          modifier = Modifier.weight(1f),
          containerColor = MaterialTheme.colorScheme.background,
          topBar = {
            AnimatedContent(targetState = selectedPage) { page ->
              when (page) {
                AppPage.MAP -> {
                  MapTopBar(vehiclesCount = mapScreenState.vehicleCount)
                }
                AppPage.LINES -> {
                  LinesTopBar(
                    onBackClick = {
                      selectedPage = AppPage.MAP
                    }
                  )
                }
              }
            }
          },
          bottomBar = {
            if (!useNavigationRail) {
              AppBottomBar(currentPage = selectedPage, onPageSelected = onPageSelected)
            }
          },
          snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
          floatingActionButton = {
            MapCenterFab(onClick = mapScreenState::centerOnVehicles)
          },
        ) { paddingValues ->
          MapScreenContent(
            state = mapScreenState,
            modifier = Modifier.fillMaxSize().padding(paddingValues),
          )
        }
      }
    }
  }
}

private enum class AppPage {
  MAP,
  LINES,
}

@Composable
private fun AppNavigationRail(
  currentPage: AppPage,
  onPageSelected: (AppPage) -> Unit,
) {
  NavigationRail {
    NavigationRailItem(
      selected = currentPage == AppPage.MAP,
      onClick = { onPageSelected(AppPage.MAP) },
      icon = { Icon(imageVector = Icons.Default.Map, contentDescription = null) },
      label = { Text(text = stringResource(Res.string.map_navigation_label)) },
    )
    NavigationRailItem(
      selected = currentPage == AppPage.LINES,
      onClick = { onPageSelected(AppPage.LINES) },
      icon = { Icon(imageVector = Icons.Default.GridView, contentDescription = null) },
      label = { Text(text = stringResource(Res.string.lines_navigation_label)) },
    )
  }
}

@Composable
private fun AppBottomBar(
  currentPage: AppPage,
  onPageSelected: (AppPage) -> Unit,
) {
  ShortNavigationBar {
    ShortNavigationBarItem(
      selected = currentPage == AppPage.MAP,
      onClick = { onPageSelected(AppPage.MAP) },
      icon = { Icon(imageVector = Icons.Default.Map, contentDescription = null) },
      label = { Text(text = stringResource(Res.string.map_navigation_label)) },
    )
    ShortNavigationBarItem(
      selected = currentPage == AppPage.LINES,
      onClick = { onPageSelected(AppPage.LINES) },
      icon = { Icon(imageVector = Icons.Default.GridView, contentDescription = null) },
      label = { Text(text = stringResource(Res.string.lines_navigation_label)) },
    )
  }
}

@Composable
private fun shouldUseNavigationRail(): Boolean {
  val (width, height) = LocalWindowInfo.current.containerSize
  val windowSize = with(LocalDensity.current) { DpSize(width.toDp(), height.toDp()) }
  @OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
  val windowSizeClass = WindowSizeClass.calculateFromSize(windowSize)
  return windowSizeClass.heightSizeClass == WindowHeightSizeClass.Compact
}
