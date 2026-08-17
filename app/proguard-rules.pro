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

# --- Firebase / GMS ---
# Firebase i Play Services dołączają własne consumer-rules.pro wewnątrz AAR,
# więc ręczny -keep na cały pakiet nie jest potrzebny (i tylko psuł optymalizację).
-dontwarn com.google.firebase.**
-dontwarn com.google.android.gms.**

# --- Credential Manager / Google Identity ---
# Podobnie jak wyżej - biblioteki androidx same dostarczają swoje reguły.
-keep class com.google.android.libraries.identity.googleid.** { *; }

# --- Firestore model classes (kept for reflection) ---
-keepclassmembers class com.philornot.slownikjezykatrudnego.data.model.** {
    *;
}

# --- WorkManager / Room (Fix for NoSuchMethodException in WorkDatabase_Impl) ---
-dontwarn androidx.room.**
#noinspection ShrinkerUnresolvedReference
-keep class * extends androidx.room.RoomDatabase {
    <init>();
}
-keep class androidx.work.impl.WorkDatabase_Impl {
    public <init>();
}