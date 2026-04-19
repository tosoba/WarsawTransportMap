package com.trm.warsawtransportmap.core.data

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import org.koin.core.module.Module
import org.koin.dsl.module

private class AppLifecycleOwner : LifecycleOwner {
  override val lifecycle: Lifecycle = LifecycleRegistry(this)
}

actual fun platformModule(): Module = module { single<Lifecycle> { AppLifecycleOwner().lifecycle } }
