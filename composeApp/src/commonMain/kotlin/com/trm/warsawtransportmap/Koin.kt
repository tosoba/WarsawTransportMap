package com.trm.warsawtransportmap

import com.trm.warsawtransportmap.core.common.coreCommonModule
import com.trm.warsawtransportmap.core.data.coreDataModule
import com.trm.warsawtransportmap.core.datastore.coreDataStoreModule
import com.trm.warsawtransportmap.core.network.di.coreNetworkModule
import com.trm.warsawtransportmap.feature.lines.featureLinesModule
import com.trm.warsawtransportmap.feature.map.featureMapModule

internal val appModules =
  listOf(
    coreCommonModule,
    coreDataModule,
    coreDataStoreModule,
    coreNetworkModule,
    featureLinesModule,
    featureMapModule,
  )
