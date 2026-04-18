package com.trm.warsawtransportmap

interface Platform {
  val name: String
}

expect fun getPlatform(): Platform
