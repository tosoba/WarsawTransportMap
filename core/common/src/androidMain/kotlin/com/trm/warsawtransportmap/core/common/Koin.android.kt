package com.trm.warsawtransportmap.core.common

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import org.koin.core.module.Module
import org.koin.dsl.bind
import org.koin.dsl.module

actual fun platformCommonModule(): Module = module {
  single(AppLifecycleOwner) { ProcessLifecycleOwner.get() }.bind(LifecycleOwner::class)
}
