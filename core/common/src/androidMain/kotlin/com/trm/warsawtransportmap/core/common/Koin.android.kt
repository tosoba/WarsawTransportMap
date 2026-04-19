package com.trm.warsawtransportmap.core.common

import androidx.lifecycle.ProcessLifecycleOwner
import org.koin.core.module.Module
import org.koin.dsl.module

actual fun platformCommonModule(): Module = module {
    single(AppLifecycle) { ProcessLifecycleOwner.get().lifecycle }
}
