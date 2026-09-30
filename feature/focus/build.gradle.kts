plugins {
    alias(libs.plugins.flowapp.android.feature)
}

android {
    namespace = "ru.alexey.flowapp.feature.focus"
}

dependencies {
    // Timer service and notification: ServiceCompat, NotificationCompat
    implementation(libs.androidx.core.ktx)
    // Notification permission request from the timer screen
    implementation(libs.androidx.activity.compose)
}