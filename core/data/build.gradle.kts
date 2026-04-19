plugins {
  alias(libs.plugins.kotlinMultiplatform)
  alias(libs.plugins.androidKotlinMultiplatformLibrary)
}

kotlin {
  android {
    namespace = "com.trm.warsawtransportmap.core.data"
    compileSdk = libs.versions.android.compileSdk.get().toInt()
    minSdk = libs.versions.android.minSdk.get().toInt()
  }

  iosArm64()
  iosSimulatorArm64()

  sourceSets {
    commonMain.dependencies {
      implementation(project(":core:network"))
      implementation(project(":core:model"))
      implementation(project(":core:common"))
      implementation(libs.koin.core)
      implementation(libs.kotlinx.serialization.json)
      implementation(libs.ktor.client.core)
      implementation(libs.kotlinx.coroutines.core)
      implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.6.1")
      implementation(libs.androidx.lifecycle.runtime)
    }
    androidMain.dependencies { }
  }
}
