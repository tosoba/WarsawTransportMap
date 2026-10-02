package com.trm.warsawtransportmap.feature.map

import com.trm.warsawtransportmap.core.common.AppLifecycleOwner
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val featureMapModule = module {
  viewModel { MapViewModel(get(), get(), get(AppLifecycleOwner), get()) }
}
