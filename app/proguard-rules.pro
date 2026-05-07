# ProGuard / R8 rules for CodeMD

# ── App model & enum classes ─────────────────────────────────────────────────
-keep class com.markmd.data.model.** { *; }
-keepclassmembers enum com.markmd.data.model.** { *; }

# ── Room ─────────────────────────────────────────────────────────────────────
-keep class * extends androidx.room.RoomDatabase { *; }
-keep @androidx.room.Entity class * { *; }
-keep @androidx.room.Dao interface * { *; }
-keepclassmembers class * extends androidx.room.RoomDatabase {
    public static ** INSTANCE;
    public static ** Companion;
}

# ── Hilt / Dagger ────────────────────────────────────────────────────────────
-keep class dagger.hilt.** { *; }
-keep class javax.inject.** { *; }
-keep class * extends dagger.hilt.android.HiltAndroidApp { *; }
-keep @dagger.hilt.android.lifecycle.HiltViewModel class * { *; }
-keepclasseswithmembers class * {
    @dagger.hilt.android.lifecycle.HiltViewModel <init>(...);
}

# ── DataStore / Kotlin Serialization ─────────────────────────────────────────
-keepclassmembers class kotlinx.serialization.json.** { *; }
-keep @kotlinx.serialization.Serializable class * { *; }

# ── Kotlin coroutines ─────────────────────────────────────────────────────────
-keepclassmembers class kotlinx.coroutines.** { *; }
-dontwarn kotlinx.coroutines.**

# ── Compose / Material3 ───────────────────────────────────────────────────────
-dontwarn androidx.compose.**
-keep class androidx.compose.** { *; }

# ── Coil 3 ───────────────────────────────────────────────────────────────────
-keep class coil3.** { *; }
-dontwarn coil3.**

# ── Markdown Renderer (mikepenz) ─────────────────────────────────────────────
-keep class com.mikepenz.markdown.** { *; }
-keep interface com.mikepenz.markdown.** { *; }
-dontwarn com.mikepenz.markdown.**

# ── Intellij / Jetbrains annotations (safe to ignore) ────────────────────────
-dontwarn org.jetbrains.annotations.**
-dontwarn com.intellij.**

# ── Suppress common warnings ─────────────────────────────────────────────────
-dontwarn java.lang.invoke.**
-dontwarn sun.misc.Unsafe
