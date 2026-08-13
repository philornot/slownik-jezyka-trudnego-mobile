# ProGuard / R8 rules for Słownik Języka Trudnego

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

# --- Firebase ---
-keep class com.google.firebase.** { *; }
-keep class com.google.android.gms.** { *; }
-dontwarn com.google.firebase.**
-dontwarn com.google.android.gms.**

# --- Credential Manager / Google Identity ---
-keep class androidx.credentials.** { *; }
-keep class com.google.android.libraries.identity.googleid.** { *; }

# --- Firestore model classes (kept for reflection) ---
-keepclassmembers class com.philornot.slownikjezykatrudnego.data.model.** {
    *;
}

