plugins {
  alias(libs.plugins.kotlinMultiplatform)
  alias(libs.plugins.androidKotlinMultiplatformLibrary)
  alias(libs.plugins.composeMultiplatform)
  alias(libs.plugins.composeCompiler)
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
      implementation(project(":core:model"))

      implementation(libs.androidx.lifecycle.runtime)

      implementation(libs.compose.components.resources)
      implementation(libs.compose.foundation)
      implementation(libs.compose.runtime)

      implementation(libs.koin.core)

      implementation(libs.ktor.client.core)

      implementation(libs.maplibre.compose)
    }
  }
}
