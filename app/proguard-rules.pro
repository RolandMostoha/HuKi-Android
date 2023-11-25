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

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile

# Crashlytics
-keepattributes SourceFile,LineNumberTable        # Keep file names and line numbers.
-keep public class * extends java.lang.Exception  # Optional: Keep custom exceptions.

-dontwarn org.bouncycastle.jsse.BCSSLSocket
-dontwarn org.bouncycastle.jsse.provider.BouncyCastleJsseProvider
-dontwarn org.conscrypt.Conscrypt.Version
-dontwarn org.conscrypt.Conscrypt
-dontwarn org.conscrypt.ConscryptHostnameVerifier
-dontwarn org.openjsse.javax.net.ssl.SSLParameters
-dontwarn org.openjsse.javax.net.ssl.SSLSocket
-dontwarn org.openjsse.net.ssl.OpenJSSE

-dontwarn org.jetbrains.annotations.**
-keep class kotlin.Metadata { *; }

-keep class hu.mostoha.mobile.android.huki.model.** { *; }
-keepclassmembers class hu.mostoha.mobile.android.huki.model.** { *; }

# AWS
-keep class com.amazonaws.mobile.** { *; }
-keepclassmembers enum * { *; }
-dontwarn com.amazonaws.mobile.auth.facebook.FacebookButton
-dontwarn com.amazonaws.mobile.auth.facebook.FacebookSignInProvider
-dontwarn com.amazonaws.mobile.auth.google.GoogleButton
-dontwarn com.amazonaws.mobile.auth.google.GoogleSignInProvider
-dontwarn com.amazonaws.mobile.auth.ui.AuthUIConfiguration$Builder
-dontwarn com.amazonaws.mobile.auth.ui.AuthUIConfiguration
-dontwarn com.amazonaws.mobile.auth.ui.SignInUI$LoginBuilder
-dontwarn com.amazonaws.mobile.auth.ui.SignInUI
-dontwarn com.amazonaws.mobile.auth.userpools.CognitoUserPoolsSignInProvider
-dontwarn kotlinx.parcelize.Parcelize

# Moshi
-keep,allowobfuscation,allowshrinking class com.squareup.moshi.JsonAdapter
-keep @com.squareup.moshi.JsonQualifier interface *
-keepclassmembers @com.squareup.moshi.JsonClass class * extends java.lang.Enum {
    <fields>;
    **[] values();
}
-keepclassmembers class com.squareup.moshi.internal.Util {
    private static java.lang.String getKotlinMetadataClassName();
}
-keepclassmembers class * {
    @com.squareup.moshi.FromJson <methods>;
    @com.squareup.moshi.ToJson <methods>;
}

# GSON
-keepattributes Signature
-keep class com.google.gson.reflect.TypeToken { *; }
-keep class * extends com.google.gson.reflect.TypeToken
-keepattributes AnnotationDefault,RuntimeVisibleAnnotations

# Retrofit
 -keep,allowobfuscation,allowshrinking interface retrofit2.Call
 -keep,allowobfuscation,allowshrinking class retrofit2.Response
 -keep,allowobfuscation,allowshrinking class kotlin.coroutines.Continuation

 # Google
 -keep class com.google.android.gms.** { *; }
 -dontwarn com.google.android.gms.*
 -keep class com.google.api.client.** {*;}