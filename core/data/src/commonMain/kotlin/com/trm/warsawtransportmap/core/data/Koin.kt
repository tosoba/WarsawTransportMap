package com.trm.warsawtransportmap.core.data

import com.trm.warsawtransportmap.core.network.di.networkModule
import org.koin.core.module.Module
import org.koin.dsl.module

expect fun platformModule(): Module

val coreDataModule = module {
  includes(platformModule(), networkModule)
  single<TransportRepository> { TransportRepositoryImpl(apiClient = get()) }
}
