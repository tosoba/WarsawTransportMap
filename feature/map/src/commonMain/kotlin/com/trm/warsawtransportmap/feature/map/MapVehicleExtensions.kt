package com.trm.warsawtransportmap.feature.map

import com.trm.warsawtransportmap.core.model.Vehicle
import kotlin.time.Clock
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import org.jetbrains.compose.resources.getPluralString
import org.jetbrains.compose.resources.getString
import warsawtransportmap.feature.map.generated.resources.Res
import warsawtransportmap.feature.map.generated.resources.vehicle_updated_minutes_ago
import warsawtransportmap.feature.map.generated.resources.vehicle_updated_now
import warsawtransportmap.feature.map.generated.resources.vehicle_updated_seconds_ago

suspend fun Vehicle.toUpdateTimeMessage(): String? = runCatching {
  val duration =
    Clock.System.now() -
      LocalDateTime.parse(time.replace(" ", "T")).toInstant(TimeZone.currentSystemDefault())
  when {
    duration.inWholeMinutes > 0 -> {
      getPluralString(
        Res.plurals.vehicle_updated_minutes_ago,
        duration.inWholeMinutes.toInt(),
        duration.inWholeMinutes,
      )
    }
    duration.inWholeSeconds > 10 -> {
      getPluralString(
        Res.plurals.vehicle_updated_seconds_ago,
        duration.inWholeSeconds.toInt(),
        duration.inWholeSeconds,
      )
    }
    else -> {
      getString(Res.string.vehicle_updated_now)
    }
  }
}
  .getOrNull()
