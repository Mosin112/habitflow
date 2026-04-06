-keepattributes SourceFile,LineNumberTable
-keep public class * extends java.lang.Exception

# Room
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *

# Hilt
-keepclasseswithmembernames class * { @dagger.* <fields>; }
-keepclasseswithmembernames class * { @dagger.* <methods>; }

# Compose
-dontwarn androidx.compose.**

# Gson
-keepattributes Signature
-keepattributes *Annotation*
-dontwarn sun.misc.**
-keep class com.google.gson.** { *; }
