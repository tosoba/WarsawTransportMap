package com.trm.warsawtransportmap.core.domain

import com.trm.warsawtransportmap.core.model.CameraPosition
import kotlinx.coroutines.flow.Flow

interface PreferencesRepository {
  val cameraPosition: Flow<CameraPosition?>
  val selectedLines: Flow<Set<String>?>

  suspend fun saveCameraPosition(cameraPosition: CameraPosition)
  suspend fun saveSelectedLines(selectedLines: Set<String>)
}
