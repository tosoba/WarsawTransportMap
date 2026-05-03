# Add project-specific Proguard rules here.
# By default, the rules in this file are only applied to the app module.

# Compose Multiplatform Resources
-keep public class org.jetbrains.compose.resources.CompiledResourceSchema { *; }
-keep class **.generated.resources.** { *; }

# Keep internal resource loading classes
-keep class org.jetbrains.compose.resources.** { *; }
-keep class org.jetbrains.compose.resources.ResourceReader { *; }
-keep class org.jetbrains.compose.resources.DefaultAndroidResourceReader { *; }

# Prevent stripping of assets
-keepclassmembers class **.generated.resources.Res$* {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}
