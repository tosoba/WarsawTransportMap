plugins {
  alias(libs.plugins.kotlinMultiplatform)
  alias(libs.plugins.androidKotlinMultiplatformLibrary)
  alias(libs.plugins.composeMultiplatform)
  alias(libs.plugins.composeCompiler)
  alias(libs.plugins.kotlinSerialization)
  alias(libs.plugins.mokoResources)
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

      export(project(":core:common"))
      export(project(":feature:map"))
      export(project(":feature:lines"))
      export(libs.moko.resources)
    }
  }

  sourceSets {
    androidMain.dependencies {
      implementation(libs.androidx.activity.compose)

      implementation(libs.compose.uiToolingPreview)
    }

    commonMain.dependencies {
      api(project(":core:common"))
      implementation(project(":core:data"))
      implementation(project(":core:datastore"))
      implementation(project(":core:domain"))
      implementation(project(":core:network"))
      implementation(project(":core:model"))
      api(project(":feature:map"))
      api(project(":feature:lines"))

      api(libs.moko.resources)
      api(libs.moko.resources.compose)

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

multiplatformResources {
  resourcesPackage.set("com.trm.warsawtransportmap.composeapp")
  resourcesClassName.set("ComposeAppMR")
  resourcesVisibility.set(dev.icerock.gradle.MRVisibility.Public)
}
