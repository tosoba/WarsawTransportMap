plugins {
  alias(libs.plugins.kotlinMultiplatform)
  alias(libs.plugins.androidKotlinMultiplatformLibrary)
}

kotlin {
  android {
    namespace = "com.trm.warsawtransportmap.core.datastore"
    compileSdk = libs.versions.android.compileSdk.get().toInt()
    minSdk = libs.versions.android.minSdk.get().toInt()
  }

  iosArm64()
  iosSimulatorArm64()

  sourceSets {
    commonMain.dependencies {
      api(libs.androidx.datastore)
      api(libs.androidx.datastore.preferences)
      implementation(libs.okio)
      implementation(libs.koin.core)
    }

    val androidMain by getting {
      dependencies {
        implementation(libs.koin.android)
      }
    }
  }
}
