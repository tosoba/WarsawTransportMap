package com.trm.warsawtransportmap.core.data

import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.bind
import org.koin.dsl.module

val coreDataModule = module {
  factoryOf(::TransportNetworkRepository).bind(TransportRepository::class)
}
