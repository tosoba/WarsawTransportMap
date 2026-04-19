package com.trm.warsawtransportmap

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.trm.warsawtransportmap.core.common.coreCommonModule
import com.trm.warsawtransportmap.core.data.coreDataModule
import com.trm.warsawtransportmap.core.network.di.coreNetworkModule
import com.trm.warsawtransportmap.feature.map.MapScreen
import com.trm.warsawtransportmap.feature.map.featureMapModule
import org.koin.compose.KoinApplication
import org.koin.dsl.KoinConfiguration

@Composable
@Preview
fun App() {
  KoinApplication(
    configuration =
      KoinConfiguration {
        modules(coreCommonModule, coreNetworkModule, coreDataModule, featureMapModule)
      }
  ) {
    MaterialTheme { MapScreen() }
  }
}
