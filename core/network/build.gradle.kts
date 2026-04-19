plugins {
  alias(libs.plugins.kotlinMultiplatform)
  alias(libs.plugins.androidKotlinMultiplatformLibrary)
  alias(libs.plugins.kotlinSerialization)
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
      implementation(libs.ktor.client.core)
      implementation(libs.ktor.client.content.negotiation)
      implementation(libs.ktor.serialization.kotlinx.json)
      implementation(libs.kotlinx.serialization.json)
    }
    androidMain.dependencies { implementation(libs.ktor.client.okhttp) }
    iosMain.dependencies { implementation(libs.ktor.client.darwin) }
  }
}
