# AutostradaAuctions ProGuard Configuration for Production

# Keep line numbers for debugging crash reports
-keepattributes SourceFile,LineNumberTable

# Keep Retrofit interfaces and models
-keep interface com.example.autostradaauctions.data.api.** { *; }
-keep class com.example.autostradaauctions.data.model.** { *; }

# Keep Gson models
-keepclassmembers,allowobfuscation class * {
  @com.google.gson.annotations.SerializedName <fields>;
}

# Keep SignalR classes
-keep class com.microsoft.signalr.** { *; }

# Keep Compose and AndroidX classes
-keep class androidx.compose.** { *; }
-keep class androidx.lifecycle.** { *; }

# Keep ViewModels
-keep class com.example.autostradaauctions.ui.viewmodel.** { *; }

# Keep data classes used in API responses
-keep class * implements android.os.Parcelable {
  public static final android.os.Parcelable$Creator *;
}

# Retrofit and OkHttp
-keepattributes Signature, InnerClasses, EnclosingMethod
-keepattributes RuntimeVisibleAnnotations, RuntimeVisibleParameterAnnotations
-keepclassmembers,allowshrinking,allowobfuscation interface * {
    @retrofit2.http.* <methods>;
}
-dontwarn org.bouncycastle.**
-dontwarn org.conscrypt.**
-dontwarn org.openjsse.**

# Coroutines
-keepclassmembernames class kotlinx.** {
    volatile <fields>;
}

# Security - obfuscate but keep functionality
-keep class com.example.autostradaauctions.security.** { *; }
-keep class com.example.autostradaauctions.data.auth.** { *; }

# Remove logging in production
-assumenosideeffects class android.util.Log {
    public static boolean isLoggable(java.lang.String, int);
    public static int v(...);
    public static int i(...);
    public static int w(...);
    public static int d(...);
    public static int e(...);
}