package com.trm.warsawtransportmap.core.common.extensions

import androidx.compose.runtime.Composable
import com.trm.warsawtransportmap.core.common.CoreCommonMR
import com.trm.warsawtransportmap.core.common.error_http
import com.trm.warsawtransportmap.core.common.error_network
import com.trm.warsawtransportmap.core.common.error_unknown
import dev.icerock.moko.resources.compose.stringResource
import io.ktor.client.plugins.ResponseException
import kotlinx.io.IOException

@Composable
fun Throwable.toErrorMessage(): String =
  when (this) {
    is IOException -> stringResource(CoreCommonMR.strings.error_network)
    is ResponseException -> stringResource(CoreCommonMR.strings.error_http, response.status.value)
    else -> stringResource(CoreCommonMR.strings.error_unknown)
  }

@Composable fun Throwable.toErrorStringResource(): String = toErrorMessage()
