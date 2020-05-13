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

# 不需要保留ViewById字段，如果有其它地方用到了会自动保留
#-keepclassmembers class ** {
#    @com.ulfy.android.ui_injection.ViewById <fields>;
#}
# 必须保留View点击方法，因为这种方法通常不会被其它地方引用
-keepclassmembers class ** {
    @com.ulfy.android.ui_injection.ViewClick <methods>;
}