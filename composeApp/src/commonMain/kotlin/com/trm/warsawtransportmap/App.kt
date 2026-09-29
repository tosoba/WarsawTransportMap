package com.trm.warsawtransportmap

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.text.input.rememberTextFieldState
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.unit.DpSize
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.trm.warsawtransportmap.core.common.coreCommonModule
import com.trm.warsawtransportmap.core.common.extensions.toErrorMessage
import com.trm.warsawtransportmap.core.common.model.Loadable
import com.trm.warsawtransportmap.core.data.coreDataModule
import com.trm.warsawtransportmap.core.datastore.coreDataStoreModule
import com.trm.warsawtransportmap.core.model.Vehicle
import com.trm.warsawtransportmap.core.network.di.coreNetworkModule
import com.trm.warsawtransportmap.feature.lines.LinesPage
import com.trm.warsawtransportmap.feature.lines.LinesToggleAllFab
import com.trm.warsawtransportmap.feature.lines.LinesTopBar
import com.trm.warsawtransportmap.feature.lines.LinesViewModel
import com.trm.warsawtransportmap.feature.lines.featureLinesModule
import com.trm.warsawtransportmap.feature.map.MapCenterVehiclesBoundingBoxFab
import com.trm.warsawtransportmap.feature.map.MapPage
import com.trm.warsawtransportmap.feature.map.MapTopBar
import com.trm.warsawtransportmap.feature.map.MapViewModel
import com.trm.warsawtransportmap.feature.map.featureMapModule
import com.trm.warsawtransportmap.feature.map.rememberMapPageState
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import org.jetbrains.compose.resources.getPluralString
import org.jetbrains.compose.resources.getString
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.KoinApplication
import org.koin.compose.viewmodel.koinViewModel
import org.koin.dsl.KoinConfiguration
import warsawtransportmap.composeapp.generated.resources.Res
import warsawtransportmap.composeapp.generated.resources.lines_navigation_label
import warsawtransportmap.composeapp.generated.resources.map_navigation_label
import warsawtransportmap.composeapp.generated.resources.vehicle_updated_minutes_ago
import warsawtransportmap.composeapp.generated.resources.vehicle_updated_now
import warsawtransportmap.composeapp.generated.resources.vehicle_updated_seconds_ago
import kotlin.time.Clock

@Composable
fun App() {
  KoinApplication(
    configuration =
      KoinConfiguration {
        modules(
          coreCommonModule,
          coreDataModule,
          coreDataStoreModule,
          coreNetworkModule,
          featureLinesModule,
          featureMapModule,
        )
      }
  ) {
    AppTheme {
      val pagerState = rememberPagerState(pageCount = AppPage.entries::size)
      val scope = rememberCoroutineScope()
      val snackbarHostState = remember(::SnackbarHostState)
      val useNavigationRail = shouldUseNavigationRail()
      val currentPage = AppPage.entries[pagerState.currentPage]
      val onPageSelected: (AppPage) -> Unit = { page ->
        scope.launch { pagerState.animateScrollToPage(page.pagerIndex) }
      }

      val mapViewModel = koinViewModel<MapViewModel>()
      val vehicles by mapViewModel.vehicles.collectAsStateWithLifecycle()
      val mapPageState =
        rememberMapPageState(
          vehicles = vehicles,
          initialCameraPosition =
            mapViewModel.initialCameraPosition
              .collectAsStateWithLifecycle(initialValue = null)
              .value,
          onCameraPositionChange = mapViewModel::onSavedCameraPositionChange,
          onVehicleClick = { vehicle ->
            scope.launch {
              val message = vehicle.toUpdateTimeMessage() ?: return@launch
              snackbarHostState.currentSnackbarData?.dismiss()
              snackbarHostState.showSnackbar(message)
            }
          },
        )

      val linesViewModel = koinViewModel<LinesViewModel>()
      val linesTextFieldState = rememberTextFieldState()

      Row(modifier = Modifier.fillMaxSize()) {
        if (useNavigationRail) {
          AppNavigationRail(currentPage = currentPage, onPageSelected = onPageSelected)
        }

        Scaffold(
          modifier = Modifier.weight(1f),
          containerColor = MaterialTheme.colorScheme.background,
          topBar = {
            AnimatedContent(currentPage) {
              when (it) {
                AppPage.MAP -> {
                  MapTopBar(vehiclesCount = vehicles.size)
                }
                AppPage.LINES -> {
                  LinesTopBar(
                    textFieldState = linesTextFieldState,
                    isLoading = linesViewModel.state is Loadable.Loading,
                  )
                }
              }
            }
          },
          bottomBar = {
            if (!useNavigationRail) {
              AppBottomBar(currentPage = currentPage, onPageSelected = onPageSelected)
            }
          },
          snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
          floatingActionButton = {
            AnimatedContent(currentPage) { page ->
              when (page) {
                AppPage.MAP -> {
                  MapCenterVehiclesBoundingBoxFab(
                    onClick = {
                      scope.launch { mapPageState.animateToVehiclesBoundingBox() }
                    }
                  )
                }
                AppPage.LINES -> {
                  LinesToggleAllFab()
                }
              }
            }
          },
        ) { paddingValues ->
          HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize().padding(paddingValues),
            userScrollEnabled = false,
            beyondViewportPageCount = 1,
          ) { page ->
            when (AppPage.entries[page]) {
              AppPage.MAP -> {
                val isLoadingVehicles by
                  mapViewModel.isLoadingVehicles.collectAsStateWithLifecycle()

                LaunchedEffect(mapViewModel.errors) {
                  mapViewModel.errors.collectLatest { error ->
                    snackbarHostState.currentSnackbarData?.dismiss()
                    snackbarHostState.showSnackbar(message = error.toErrorMessage())
                  }
                }

                MapPage(
                  state = mapPageState.mapState,
                  isLoadingVehicles = isLoadingVehicles,
                  modifier = Modifier.fillMaxSize(),
                )
              }
              AppPage.LINES -> {
                LinesPage(
                  query = linesTextFieldState.text.toString(),
                  modifier = Modifier.fillMaxSize(),
                )
              }
            }
          }
        }
      }
    }
  }
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
  val windowInfo = LocalWindowInfo.current
  val windowSize =
    with(LocalDensity.current) {
      DpSize(windowInfo.containerSize.width.toDp(), windowInfo.containerSize.height.toDp())
    }
  @OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
  val windowSizeClass = WindowSizeClass.calculateFromSize(windowSize)
  return windowSizeClass.heightSizeClass == WindowHeightSizeClass.Compact
}

private suspend fun Vehicle.toUpdateTimeMessage(): String? = runCatching {
  val duration =
    Clock.System.now() -
      LocalDateTime.parse(time.replace(" ", "T")).toInstant(TimeZone.currentSystemDefault())
  when {
    duration.inWholeMinutes > 0 -> {
      getPluralString(
        Res.plurals.vehicle_updated_minutes_ago,
        duration.inWholeMinutes.toInt(),
        duration.inWholeMinutes,
      )
    }
    duration.inWholeSeconds > 10 -> {
      getPluralString(
        Res.plurals.vehicle_updated_seconds_ago,
        duration.inWholeSeconds.toInt(),
        duration.inWholeSeconds,
      )
    }
    else -> {
      getString(Res.string.vehicle_updated_now)
    }
  }
}
  .getOrNull()
