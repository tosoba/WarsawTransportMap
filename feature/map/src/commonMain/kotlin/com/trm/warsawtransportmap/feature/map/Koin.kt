package com.trm.warsawtransportmap.feature.map

import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val featureMapModule = module { viewModel { MapViewModel(get(), get(), get()) } }
