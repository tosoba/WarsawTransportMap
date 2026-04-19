plugins {
  alias(libs.plugins.kotlinMultiplatform)
  alias(libs.plugins.androidKotlinMultiplatformLibrary)
  alias(libs.plugins.composeMultiplatform)
  alias(libs.plugins.composeCompiler)
}

kotlin {
  android {
    namespace = "com.trm.warsawtransportmap.feature.map"
    compileSdk = libs.versions.android.compileSdk.get().toInt()
    minSdk = libs.versions.android.minSdk.get().toInt()
  }

  iosArm64()
  iosSimulatorArm64()

  sourceSets {
    commonMain.dependencies {
      implementation(project(":core:data"))
      implementation(project(":core:model"))
      implementation(project(":core:common"))
      implementation(libs.androidx.lifecycle.viewmodelCompose)
      implementation(libs.koin.core)
      implementation(libs.koin.compose)
      implementation(libs.koin.compose.viewmodel)
      implementation(libs.maplibre.compose)
      implementation(libs.kotlinx.datetime)
      implementation(libs.androidx.lifecycle.runtime)
      implementation(libs.androidx.lifecycle.runtimeCompose)

      implementation(libs.compose.ui)
      implementation(libs.compose.foundation)
      implementation(libs.compose.material3)
      implementation(libs.compose.runtime)
    }
  }
}
