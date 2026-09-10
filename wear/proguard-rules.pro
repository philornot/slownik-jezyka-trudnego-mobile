# ProGuard / R8 rules for Słownik Języka Trudnego (Wear OS)

# --- kotlinx.serialization ---
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt
-keepclassmembers class kotlinx.serialization.json.** {
    *** Companion;
}
-keepclasseswithmembers class * {
    @kotlinx.serialization.Serializable <fields>;
}
-keep,includedescriptorclasses class com.philornot.slownikjezykatrudnego.**$$serializer { *; }
-keepclassmembers class com.philornot.slownikjezykatrudnego.** {
    *** Companion;
}
-keepclasseswithmembers class com.philornot.slownikjezykatrudnego.** {
    kotlinx.serialization.KSerializer serializer(...);
}

# --- Dictionary models ---
-keepclassmembers class com.philornot.slownikjezykatrudnego.data.model.** {
    *;
}
