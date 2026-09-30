plugins {
    alias(libs.plugins.flowapp.jvm.library)
    alias(libs.plugins.koin.compiler)
}

dependencies {
    api(project(":core:model"))
    api(project(":core:common"))
    implementation(libs.koin.core)
    implementation(libs.koin.annotations)

    testImplementation(libs.turbine)
    testImplementation(libs.mockk)
}