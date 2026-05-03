package com.trm.warsawtransportmap.core.common.model

sealed interface Loadable<out T> {
  data object Loading : Loadable<Nothing>

  data class Loaded<T>(val data: T) : Loadable<T>

  data class Error(val throwable: Throwable) : Loadable<Nothing>
}
