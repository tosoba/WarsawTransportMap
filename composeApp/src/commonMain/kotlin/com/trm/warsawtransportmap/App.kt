package com.trm.warsawtransportmap

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.dropUnlessResumed
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import androidx.savedstate.serialization.SavedStateConfiguration
import com.trm.warsawtransportmap.core.common.coreCommonModule
import com.trm.warsawtransportmap.core.data.coreDataModule
import com.trm.warsawtransportmap.core.datastore.dataStoreModule
import com.trm.warsawtransportmap.core.network.di.coreNetworkModule
import com.trm.warsawtransportmap.feature.lines.LinesScreen
import com.trm.warsawtransportmap.feature.lines.featureLinesModule
import com.trm.warsawtransportmap.feature.map.MapScreen
import com.trm.warsawtransportmap.feature.map.featureMapModule
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import org.koin.compose.KoinApplication
import org.koin.core.KoinApplication as KoinApp
import org.koin.dsl.KoinConfiguration

@Composable
@Preview
fun App(koinConfiguration: KoinApp.() -> Unit = {}) {
  KoinApplication(
    configuration =
      KoinConfiguration {
        modules(
          coreCommonModule,
          coreDataModule,
          dataStoreModule,
          coreNetworkModule,
          featureMapModule,
          featureLinesModule,
        )
        koinConfiguration()
      }
  ) {
    val backStack =
      rememberNavBackStack(
        configuration =
          SavedStateConfiguration {
            serializersModule = SerializersModule {
              polymorphic(NavKey::class) {
                subclass(MapNavKey::class, MapNavKey.serializer())
                subclass(LinesNavKey::class, LinesNavKey.serializer())
              }
            }
          },
        MapNavKey,
      )
    MaterialTheme {
      NavDisplay(
        backStack = backStack,
        entryProvider =
          entryProvider {
            entry<MapNavKey> {
              MapScreen(onNavigateToLines = dropUnlessResumed { backStack.add(LinesNavKey) })
            }
            entry<LinesNavKey> { LinesScreen() }
          },
      )
    }
  }
}
