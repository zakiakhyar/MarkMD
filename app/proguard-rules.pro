# ProGuard rules for MarkMD

# Keep model classes
-keep class com.markmd.data.model.** { *; }
-keep class com.markmd.domain.model.** { *; }

# Keep Room entities
-keep class * extends androidx.room.RoomDatabase { *; }
-keep @androidx.room.Entity class * { *; }
-keep @androidx.room.Dao class * { *; }

# Keep Hilt components
-keep class * extends dagger.hilt.android.HiltAndroidApp { *; }
-keep class * extends android.app.Application { *; }

# Coil
-keep class coil3.** { *; }

# Markdown Renderer
-keep class com.mikepenz.markdown.** { *; }
