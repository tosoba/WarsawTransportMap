# WarsawTransportMap

Kotlin Multiplatform project (Android + iOS) using Jetpack Compose.

## Build Commands

- Android: `./gradlew :composeApp:assembleDebug`
- iOS: Open `iosApp/` in Xcode and run

## Project Structure

- `composeApp/` - shared Kotlin multiplatform library (commonMain, androidMain, iosMain)
- `androidApp/` - Android application entry point
- `iosApp/` - iOS Swift app entry point

## Key Files

- `build.gradle.kts` - root build (plugins only)
- `composeApp/build.gradle.kts` - shared library config
- `androidApp/build.gradle.kts` - Android app config
- `gradle/libs.versions.toml` - dependency version catalog
