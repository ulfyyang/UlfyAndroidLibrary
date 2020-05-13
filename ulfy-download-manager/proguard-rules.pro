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

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile

# 保留Serializable序列化
-keep class * implements java.io.Serializable { *; }
# 保留DownloadTaskInfo
-keep public interface com.ulfy.android.download_manager.DownloadTaskInfo { *; }
# 因为不知道okdownload的具体规则，因此保留全部
-keep class com.liulishuo.okdownload.** { *; }