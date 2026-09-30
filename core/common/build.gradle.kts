plugins {
    alias(libs.plugins.flowapp.jvm.library)
}

dependencies {
    api(project(":core:model"))
    api(libs.kotlinx.coroutines.android)
    implementation(libs.koin.core)
    implementation(libs.koin.annotations)
}