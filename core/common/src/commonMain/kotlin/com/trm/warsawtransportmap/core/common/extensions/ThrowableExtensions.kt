package com.trm.warsawtransportmap.core.common.extensions

import androidx.compose.runtime.Composable
import io.ktor.client.plugins.ResponseException
import kotlinx.io.IOException
import org.jetbrains.compose.resources.getString
import org.jetbrains.compose.resources.stringResource
import warsawtransportmap.core.common.generated.resources.Res
import warsawtransportmap.core.common.generated.resources.error_http
import warsawtransportmap.core.common.generated.resources.error_network
import warsawtransportmap.core.common.generated.resources.error_unknown

suspend fun Throwable.toErrorMessage(): String =
  when (this) {
    is IOException -> getString(Res.string.error_network)
    is ResponseException -> getString(Res.string.error_http, response.status.value)
    else -> getString(Res.string.error_unknown)
  }

@Composable
fun Throwable.toErrorStringResource(): String =
  when (this) {
    is IOException -> stringResource(Res.string.error_network)
    is ResponseException -> stringResource(Res.string.error_http, response.status.value)
    else -> stringResource(Res.string.error_unknown)
  }
