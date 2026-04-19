package com.trm.warsawtransportmap.core.data

import org.koin.dsl.module

val coreDataModule = module {
  single<TransportRepository> { TransportRepositoryImpl(apiClient = get()) }
}
