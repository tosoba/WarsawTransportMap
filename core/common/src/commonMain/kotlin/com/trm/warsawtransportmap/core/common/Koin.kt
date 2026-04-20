package com.trm.warsawtransportmap.core.common

import org.koin.core.module.Module
import org.koin.core.qualifier.named
import org.koin.dsl.module

val AppLifecycle = named("AppLifecycle")

expect fun platformCommonModule(): Module

val coreCommonModule = module { includes(platformCommonModule()) }
