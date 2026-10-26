# Keep JavascriptInterface methods for WebView bridge
-keepattributes JavascriptInterface
-keepclassmembers class * {
    @android.webkit.JavascriptInterface <methods>;
}

# Keep Data Models for Gson reflection
-keep class com.jobautoapply.app.data.model.** { *; }
-keepclassmembers class com.jobautoapply.app.data.model.** { *; }

# Keep WebKit and Coroutines internal metadata
-keepattributes *Annotation*,Signature,InnerClasses,EnclosingMethod
