# ProGuard Rules for News4Live Android App

# Kotlinx Serialization
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.SerializationKt
-keepclassmembers class * {
    @kotlinx.serialization.Serializable <fields>;
}
-keepclassmembers class * {
    @kotlinx.serialization.SerialName <fields>;
}
-keepclassmembers class **$$serializer {
    *;
}
-keepclasseswithmembers class * {
    kotlinx.serialization.KSerializer serializer(...);
}

# Retrofit & OkHttp
-dontwarn okhttp3.**
-dontwarn okio.**
-dontwarn retrofit2.**
-keepattributes Signature
-keepattributes Exceptions
-keepclasseswithmembers class * {
    @retrofit2.http.* <methods>;
}

# Room Database
-keep class * extends androidx.room.RoomDatabase
-dontwarn androidx.room.paging.**

# Jsoup
-keep public class org.jsoup.** { public *; }

# Coil
-dontwarn coil.**
-keep class coil.** { *; }

# Models & Data Layer
-keep class com.news4live.app.data.** { *; }
-keep class com.news4live.app.domain.model.** { *; }

# Google Mobile Ads (AdMob)
-keep public class com.google.android.gms.ads.** {
    public *;
}
-keep public class com.google.ads.** {
    public *;
}
-dontwarn com.google.android.gms.ads.**
-dontwarn com.google.ads.**
