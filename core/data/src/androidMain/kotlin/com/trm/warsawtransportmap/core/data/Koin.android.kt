package com.trm.warsawtransportmap.core.data

import androidx.lifecycle.ProcessLifecycleOwner
import org.koin.core.module.Module
import org.koin.dsl.module

actual fun platformModule(): Module = module { single { ProcessLifecycleOwner.get().lifecycle } }
