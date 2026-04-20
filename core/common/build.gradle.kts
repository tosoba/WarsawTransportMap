plugins {
  alias(libs.plugins.kotlinMultiplatform)
  alias(libs.plugins.androidKotlinMultiplatformLibrary)
}

kotlin {
  android {
    namespace = "com.trm.warsawtransportmap.core.common"
    compileSdk = libs.versions.android.compileSdk.get().toInt()
    minSdk = libs.versions.android.minSdk.get().toInt()
  }

  iosArm64()
  iosSimulatorArm64()

  sourceSets {
    androidMain.dependencies { implementation(libs.androidx.lifecycle.process) }

    commonMain.dependencies {
      implementation(libs.androidx.lifecycle.runtime)
      implementation(libs.koin.core)
    }
  }
}
