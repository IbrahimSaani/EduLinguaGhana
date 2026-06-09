# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# EduLingua Ghana ProGuard Rules

# Preservation of line numbers for crash reporting
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# --- Firebase rules ---
-keep class com.google.firebase.** { *; }
-dontwarn com.google.firebase.**

# --- Firebase Realtime Database Models ---
# Preserve the model classes used for Firebase serialization
-keep class com.edulinguaghana.tracking.ProgressActivity { *; }
-keep class com.edulinguaghana.tracking.ProgressAggregate { *; }
-keep class com.edulinguaghana.tracking.StudentProgressItem { *; }
-keep class com.edulinguaghana.roles.UserRole { *; }
-keep class com.edulinguaghana.roles.UserRelationship { *; }
-keep class com.edulinguaghana.social.Challenge { *; }
-keep class com.edulinguaghana.gamification.Badge { *; }
-keep class com.edulinguaghana.gamification.Quest { *; }
-keep class com.edulinguaghana.Achievement { *; }

# --- Lottie rules ---
-keep class com.airbnb.lottie.** { *; }

# --- Facebook SDK rules ---
-keep class com.facebook.** { *; }
-dontwarn com.facebook.**

# --- Keep methods with specific annotations ---
-keepattributes *Annotation*
-keepclassmembers class * {
    @com.google.firebase.database.PropertyName *;
    @com.google.firebase.database.IgnoreExtraProperties *;
    @com.google.firebase.database.Exclude *;
}

# --- Preserve names for JNI or Reflection if needed ---
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

# --- Gson rules ---
-keepattributes Signature
-keepattributes *Annotation*
-dontwarn sun.misc.**
-keep class com.google.gson.** { *; }
-keep class com.edulinguaghana.** { *; }

# --- Strip debug logs in production ---
-assumenosideeffects class android.util.Log {
    public static int v(...);
    public static int d(...);
    public static int i(...);
}

# --- Glide rules ---
-keep public class * extends com.bumptech.glide.module.AppGlideModule
-keep public class * extends com.bumptech.glide.module.LibraryGlideModule
-keep class com.bumptech.glide.** { *; }
-keep interface com.bumptech.glide.** { *; }
-dontwarn com.bumptech.glide.**
