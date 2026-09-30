plugins {
    alias(libs.plugins.flowapp.android.library)
    alias(libs.plugins.flowapp.android.compose)
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "ru.alexey.flowapp.core.ui"
}

dependencies {
    api(project(":core:model"))
    api(project(":core:common"))
    api(project(":core:designsystem"))

    api(libs.androidx.navigation.compose)
    api(libs.kotlinx.serialization.json)

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.kotlinx.datetime)
}