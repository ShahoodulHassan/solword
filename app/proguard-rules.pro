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


-keep public class com.google.android.gms.*
-dontwarn com.google.android.gms.**
-dontwarn com.squareup.picasso.**

-keepclassmembers class com.google.android.play.* { public *; }
-keep class com.google.android.play.* {*;}
-keepclasseswithmembers class com.google.android.play.core.appupdate.* {*;}

#For removing logs
-assumenosideeffects class android.util.Log {
  public static *** d(...);
  public static *** w(...);
  public static *** v(...);
  public static *** i(...);
}


# Regular ones start from here.......

# Optimize
-optimizations !field/*,!class/merging/*,*
-mergeinterfacesaggressively

# will keep line numbers and file name obfuscation
-renamesourcefileattribute SourceFile
-keepattributes SourceFile,LineNumberTable




##---------------------------------------------------------------------


-keep class * implements android.os.Parcelable {
   public static final android.os.Parcelable$Creator *;
}

##---------------Begin: proguard configuration for Gson  ----------
## Gson uses generic type information stored in a class file when working with fields. Proguard
## removes such information by default, so configure it to keep all of it.
#-keepattributes Signature
## Keep the annotations
#-keepattributes *Annotation*

# Gson specific classes
-keep class com.google.gson.* { *; }
-keep public class com.google.gson.* {public private protected *;}
-keep class sun.misc.Unsafe.* { *; }
#-keep class com.google.gson.stream.** { *; }

-keep class com.google.gson.reflect.TypeToken { *; }
-keep class * implements com.google.gson.reflect.TypeToken { *; }

# Prevent proguard from stripping interface information from TypeAdapterFactory,
# JsonSerializer, JsonDeserializer instances (so they can be used in @JsonAdapter)
-keep class * implements com.google.gson.TypeAdapterFactory
-keep class * implements com.google.gson.JsonSerializer
-keep class * implements com.google.gson.JsonDeserializer

# Add any classes that interact with gson
# Had to use keepclasseswithmembers as mentioned in https://goo.gl/DFydHq because whenever we
# restore collections, bonds, prizes etc., the objects have null members.
-keepclasseswithmembers class com.appicacious.solword.models.* { *; }
-keepclasseswithmembers class com.appicacious.solword.utilities.* { *; }
-keepclasseswithmembers class com.appicacious.solword.tasks.* { *; }
#-keepclasseswithmembers class com.appicacious.guesswordle.workers.* { *; }


# Hide warnings about references to newer platforms in the library
-dontwarn android.support.v7.**

# don't process support library
-keep class android.support.v7.* { *; }
-keep interface android.support.v7.* { *; }

-keep public class * extends androidx.appcompat.app.AppCompatActivity
-keep public class * extends android.app.Application
-keep public class * extends android.app.MapActivity
-keep public class * extends android.app.Service
-keep public class * extends android.content.BroadcastReceiver
-keep public class * extends android.content.ContentProvider

# To support Enum type of class and members
-keep public enum * { *; }
-keepclassmembers enum * { *; }

-keep class com.activeandroid.* { *; }
-keep class com.activeandroid.*.* { *; }
-keep class * extends com.activeandroid.Model
-keep class * extends com.activeandroid.serializer.TypeSerializer

# Firebase related rules
-keep class com.firebase.** { *; }
-keep class com.google.firebase.** { *; }
-keepnames class com.fasterxml.jackson.** { *; }
-keepnames class javax.servlet.** { *; }
-keepnames class org.ietf.jgss.** { *; }
-dontwarn org.w3c.dom.**
-dontwarn org.joda.time.**
-dontwarn org.shaded.apache.**
-dontwarn org.ietf.jgss.**
-dontwarn com.firebase.**
-dontnote com.firebase.client.core.GaePlatform
-dontwarn com.firebase.ui.auth.**

# Room
-keep class android.arch.persistence.room.paging.LimitOffsetDataSource
-keep interface android.arch.persistence.room.paging.LimitOffsetDataSource
-keep class android.arch.util.paging.CountedDataSource
-keep interface android.arch.util.paging.CountedDataSource




