# Project specific ProGuard rules

# [GSON]
# Prevent GSON from obfuscating model classes to ensure JSON mapping works
-keep class net.kongbaguni.lightmetter.model.** { *; }
-keepattributes Signature
-keepattributes *Annotation*
-dontwarn sun.misc.**
-keep class com.google.gson.** { *; }

# [Room]
# Room often needs these to prevent issues with generated code
-keepclassmembers class * extends androidx.room.RoomDatabase {
    <init>(...);
}
-keep class androidx.room.introspection.DatabaseVerificationHelper { *; }

# [General]
# Keep line numbers for better stack traces in Crashlytics
-keepattributes SourceFile,LineNumberTable

# [Compose]
# Usually handled by the Compose compiler, but keeping common rules
-keep class androidx.compose.runtime.Recomposer { *; }
-keep class androidx.compose.ui.platform.AndroidComposeView { *; }
