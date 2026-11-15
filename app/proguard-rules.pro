# OtakuZone ProGuard rules
-keepattributes *Annotation*
-keepclassmembers class * {
    @com.google.gson.annotations.SerializedName <fields>;
}
-keep class com.otakuzone.app.data.models.** { *; }
-keep class androidx.media3.** { *; }
