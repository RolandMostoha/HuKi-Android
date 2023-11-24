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

-keepclassmembers class hu.mostoha.mobile.android.huki.model.** {
  <init>(...);
  <fields>;
}

# AWS
-keepclassmembers class com.amazonaws.mobile.** {
  <init>(...);
  <fields>;
}
-dontwarn com.amazonaws.mobile.auth.facebook.FacebookButton
-dontwarn com.amazonaws.mobile.auth.facebook.FacebookSignInProvider
-dontwarn com.amazonaws.mobile.auth.google.GoogleButton
-dontwarn com.amazonaws.mobile.auth.google.GoogleSignInProvider
-dontwarn com.amazonaws.mobile.auth.ui.AuthUIConfiguration$Builder
-dontwarn com.amazonaws.mobile.auth.ui.AuthUIConfiguration
-dontwarn com.amazonaws.mobile.auth.ui.SignInUI$LoginBuilder
-dontwarn com.amazonaws.mobile.auth.ui.SignInUI
-dontwarn com.amazonaws.mobile.auth.userpools.CognitoUserPoolsSignInProvider