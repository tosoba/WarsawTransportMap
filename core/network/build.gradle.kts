import java.util.Properties

plugins {
  alias(libs.plugins.kotlinMultiplatform)
  alias(libs.plugins.androidKotlinMultiplatformLibrary)
  alias(libs.plugins.kotlinSerialization)
  alias(libs.plugins.buildconfig)
}

val localProperties =
  Properties().apply {
    val file = rootProject.file("local.properties")
    if (file.exists()) {
      file.inputStream().use { load(it) }
    }
  }

buildConfig {
  useKotlinOutput { topLevelConstants = true }
  packageName("com.trm.warsawtransportmap.core.network")

  val apiKey = localProperties.getProperty("UM_API_KEY") ?: ""
  buildConfigField("UM_API_KEY", apiKey)
}

kotlin {
  android {
    namespace = "com.trm.warsawtransportmap.core.network"
    compileSdk = libs.versions.android.compileSdk.get().toInt()
    minSdk = libs.versions.android.minSdk.get().toInt()
  }

  iosArm64()
  iosSimulatorArm64()

  sourceSets {
    commonMain.dependencies {
      api(libs.ktor.client.core)
      api(libs.ktor.client.content.negotiation)
      api(libs.ktor.serialization.kotlinx.json)
      api(libs.kotlinx.serialization.json)
      api(libs.koin.core)
    }
    androidMain.dependencies { implementation(libs.ktor.client.okhttp) }
    iosMain.dependencies { implementation(libs.ktor.client.darwin) }
  }
}
