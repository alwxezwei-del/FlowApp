plugins {
    alias(libs.plugins.flowapp.android.library)
    alias(libs.plugins.flowapp.android.compose)
}

android {
    namespace = "ru.alexey.flowapp.core.designsystem"
}

dependencies {
    api(project(":core:model"))

    implementation(libs.androidx.core.ktx)
    api(libs.androidx.compose.material.icons.extended)
}