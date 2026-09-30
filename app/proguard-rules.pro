# Room
-keep class * extends androidx.room.RoomDatabase { <init>(); }

# kotlinx.serialization
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.**
-keepclassmembers class kotlinx.serialization.json.** {
    *** Companion;
}
-keepclasseswithmembers class kotlinx.serialization.json.** {
    kotlinx.serialization.KSerializer serializer(...);
}
-keep,includedescriptorclasses class ru.alexey.flowapp.**$$serializer { *; }
-keepclassmembers class ru.alexey.flowapp.** {
    *** Companion;
}
