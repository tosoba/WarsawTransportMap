package com.trm.warsawtransportmap.feature.lines

import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val featureLinesModule = module { viewModelOf(::LinesViewModel) }
