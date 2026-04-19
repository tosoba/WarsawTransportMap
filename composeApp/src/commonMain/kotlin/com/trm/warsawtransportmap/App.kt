package com.trm.warsawtransportmap

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.trm.warsawtransportmap.core.common.coreCommonModule
import com.trm.warsawtransportmap.core.data.coreDataModule
import com.trm.warsawtransportmap.core.network.di.coreNetworkModule
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
    MaterialTheme {
      Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text("Warsaw Transport Map - Data fetching initialized")
      }
    }
  }
}
