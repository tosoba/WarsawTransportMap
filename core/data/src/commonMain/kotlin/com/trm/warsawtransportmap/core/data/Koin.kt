package com.trm.warsawtransportmap.core.data

import com.trm.warsawtransportmap.core.data.preferences.PreferencesLocalRepository
import com.trm.warsawtransportmap.core.data.transport.TransportNetworkRepository
import com.trm.warsawtransportmap.core.domain.TransportRepository
import com.trm.warsawtransportmap.core.domain.PreferencesRepository
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.bind
import org.koin.dsl.module

val coreDataModule = module {
  factoryOf(::TransportNetworkRepository).bind(TransportRepository::class)
  factoryOf(::PreferencesLocalRepository).bind(PreferencesRepository::class)
}
