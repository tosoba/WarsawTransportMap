package com.trm.warsawtransportmap.feature.map

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.trm.warsawtransportmap.core.model.Vehicle
import dev.icerock.moko.resources.compose.pluralStringResource
import dev.icerock.moko.resources.compose.stringResource
import kotlin.time.Clock
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant

@Composable
fun Vehicle.toUpdateTimeMessage(): String? {
  val duration =
    remember(time) {
      runCatching {
        Clock.System.now() -
          LocalDateTime.parse(time.replace(" ", "T")).toInstant(TimeZone.currentSystemDefault())
      }
        .getOrNull()
    } ?: return null

  return when {
    duration.inWholeMinutes > 0 -> {
      pluralStringResource(
        FeatureMapMR.plurals.vehicle_updated_minutes_ago,
        duration.inWholeMinutes.toInt(),
        duration.inWholeMinutes,
      )
    }
    duration.inWholeSeconds > 10 -> {
      pluralStringResource(
        FeatureMapMR.plurals.vehicle_updated_seconds_ago,
        duration.inWholeSeconds.toInt(),
        duration.inWholeSeconds,
      )
    }
    else -> {
      stringResource(FeatureMapMR.strings.vehicle_updated_now)
    }
  }
}
