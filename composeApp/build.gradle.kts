plugins {
  alias(libs.plugins.kotlinMultiplatform)
  alias(libs.plugins.androidKotlinMultiplatformLibrary)
  alias(libs.plugins.composeMultiplatform)
  alias(libs.plugins.composeCompiler)
  alias(libs.plugins.kotlinSerialization)
}

kotlin {
  jvmToolchain(21)

  android {
    namespace = "com.trm.warsawtransportmap"
    compileSdk = libs.versions.android.compileSdk.get().toInt()
    minSdk = libs.versions.android.minSdk.get().toInt()
    androidResources { enable = true }
  }

  listOf(iosArm64(), iosSimulatorArm64()).forEach { iosTarget ->
    iosTarget.binaries.framework {
      baseName = "ComposeApp"
      isStatic = true
      export(project(":feature:lines"))
    }
  }

  sourceSets {
    androidMain.dependencies {
      implementation(libs.androidx.activity.compose)

      implementation(libs.compose.uiToolingPreview)
    }

    commonMain.dependencies {
      implementation(project(":core:common"))
      implementation(project(":core:data"))
      implementation(project(":core:datastore"))
      implementation(project(":core:domain"))
      implementation(project(":core:network"))
      implementation(project(":core:model"))
      implementation(project(":feature:map"))
      api(project(":feature:lines"))

      implementation(libs.androidx.lifecycle.runtimeCompose)
      implementation(libs.androidx.lifecycle.viewmodelCompose)

      implementation(libs.compose.components.resources)
      implementation(libs.compose.foundation)
      implementation(libs.compose.material3)
      implementation(libs.compose.material3.window.size)
      implementation(libs.compose.materialIconsExtended)
      implementation(libs.compose.runtime)
      implementation(libs.compose.ui)
      implementation(libs.compose.uiToolingPreview)

      implementation(libs.koin.core)
      implementation(libs.koin.compose)
      implementation(libs.koin.compose.viewmodel)
      implementation(libs.kotlinx.datetime)

    }

    commonTest.dependencies { implementation(libs.kotlin.test) }
  }
}
