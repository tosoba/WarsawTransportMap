plugins {
  alias(libs.plugins.kotlinMultiplatform)
  alias(libs.plugins.androidKotlinMultiplatformLibrary)
}

kotlin {
  android {
    namespace = "com.trm.warsawtransportmap.core.domain"
    compileSdk = libs.versions.android.compileSdk.get().toInt()
    minSdk = libs.versions.android.minSdk.get().toInt()
  }

  iosArm64()
  iosSimulatorArm64()

  sourceSets {
    commonMain.dependencies {
      implementation(project(":core:model"))
      implementation(libs.kotlinx.coroutines.core)
    }
  }
}
