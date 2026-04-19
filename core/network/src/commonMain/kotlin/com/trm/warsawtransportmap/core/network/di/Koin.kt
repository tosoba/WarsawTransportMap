package com.trm.warsawtransportmap.core.network.di

import com.trm.warsawtransportmap.core.network.client.UmApiClient
import com.trm.warsawtransportmap.core.network.client.umApiHttpClient
import kotlinx.serialization.json.Json
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.module

val coreNetworkModule = module {
  single { Json { ignoreUnknownKeys = true } }
  single { umApiHttpClient(get()) }
  factoryOf(::UmApiClient)
}
