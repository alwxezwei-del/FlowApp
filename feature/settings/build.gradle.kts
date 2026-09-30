plugins {
    alias(libs.plugins.flowapp.android.feature)
}

android {
    namespace = "ru.alexey.flowapp.feature.settings"
}

dependencies {
    // Export/import via Storage Access Framework
    implementation(libs.androidx.activity.compose)
}