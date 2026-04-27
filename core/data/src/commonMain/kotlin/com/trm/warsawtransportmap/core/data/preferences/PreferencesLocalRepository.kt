package com.trm.warsawtransportmap.core.data.preferences

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import com.trm.warsawtransportmap.core.domain.PreferencesRepository
import com.trm.warsawtransportmap.core.model.CameraPosition
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class PreferencesLocalRepository(private val dataStore: DataStore<Preferences>) :
  PreferencesRepository {
  override val cameraPosition: Flow<CameraPosition?> =
    dataStore.data.map { preferences ->
      val lat = preferences[LATITUDE_KEY]
      val lon = preferences[LONGITUDE_KEY]
      val zoom = preferences[ZOOM_KEY]
      if (lat != null && lon != null && zoom != null) {
        CameraPosition(lat, lon, zoom)
      } else {
        null
      }
    }

  override suspend fun saveCameraPosition(cameraPosition: CameraPosition) {
    dataStore.edit { preferences ->
      preferences[LATITUDE_KEY] = cameraPosition.latitude
      preferences[LONGITUDE_KEY] = cameraPosition.longitude
      preferences[ZOOM_KEY] = cameraPosition.zoom
    }
  }

  companion object {
    private val LATITUDE_KEY = doublePreferencesKey("camera_latitude")
    private val LONGITUDE_KEY = doublePreferencesKey("camera_longitude")
    private val ZOOM_KEY = doublePreferencesKey("camera_zoom")
  }
}
