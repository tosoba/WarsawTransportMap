package com.trm.warsawtransportmap.core.datastore

import kotlinx.cinterop.ExperimentalForeignApi
import org.koin.core.module.Module
import org.koin.dsl.module
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSURL
import platform.Foundation.NSUserDomainMask

@OptIn(ExperimentalForeignApi::class)
actual val coreDataStoreModule: Module = module {
  single {
    createDataStore {
      val documentDirectory: NSURL =
        NSFileManager.defaultManager.URLForDirectory(
          directory = NSDocumentDirectory,
          inDomain = NSUserDomainMask,
          appropriateForURL = null,
          create = false,
          error = null,
        )!!
      documentDirectory.path + "/$DATA_STORE_FILE_NAME"
    }
  }
}
