package com.trm.warsawtransportmap.feature.lines

import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val featureLinesModule = module { viewModel { LinesViewModel(get()) } }
