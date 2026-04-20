package com.trm.warsawtransportmap.core.common

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import org.koin.core.module.Module
import org.koin.dsl.module

private class AppLifecycleOwner : LifecycleOwner {
  override val lifecycle: Lifecycle = LifecycleRegistry(this)
}

actual fun platformCommonModule(): Module = module {
  single(AppLifecycle) { AppLifecycleOwner().lifecycle }
}
