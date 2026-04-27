package com.trm.warsawtransportmap.core.datastore

import org.koin.android.ext.koin.androidContext
import org.koin.core.module.Module
import org.koin.dsl.module

actual val coreDataStoreModule: Module = module {
  single {
    createDataStore { androidContext().filesDir.resolve(DATA_STORE_FILE_NAME).absolutePath }
  }
}
