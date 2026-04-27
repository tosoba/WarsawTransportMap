package com.trm.warsawtransportmap.core.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import okio.Path.Companion.toPath
import org.koin.core.module.Module

internal fun createDataStore(producePath: () -> String): DataStore<Preferences> =
  PreferenceDataStoreFactory.createWithPath(produceFile = { producePath().toPath() })

internal const val DATA_STORE_FILE_NAME = "user_prefs.preferences_pb"

expect val dataStoreModule: Module
