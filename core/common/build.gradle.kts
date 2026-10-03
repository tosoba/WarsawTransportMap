plugins {
  alias(libs.plugins.kotlinMultiplatform)
  alias(libs.plugins.androidKotlinMultiplatformLibrary)
  alias(libs.plugins.composeMultiplatform)
  alias(libs.plugins.composeCompiler)
  alias(libs.plugins.mokoResources)
}

kotlin {
  android {
    namespace = "com.trm.warsawtransportmap.core.common"
    compileSdk = libs.versions.android.compileSdk.get().toInt()
    minSdk = libs.versions.android.minSdk.get().toInt()
    androidResources { enable = true }
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

      api(libs.moko.resources)
      api(libs.moko.resources.compose)
    }
  }
}

multiplatformResources {
  resourcesPackage.set("com.trm.warsawtransportmap.core.common")
  resourcesClassName.set("CoreCommonMR")
  resourcesVisibility.set(dev.icerock.gradle.MRVisibility.Public)
}
