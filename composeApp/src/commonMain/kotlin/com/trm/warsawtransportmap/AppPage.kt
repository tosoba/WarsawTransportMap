package com.trm.warsawtransportmap

internal enum class AppPage {
  MAP,
  LINES;

  val pagerIndex: Int
    get() =
      when (this) {
        MAP -> 0
        LINES -> 1
      }
}
