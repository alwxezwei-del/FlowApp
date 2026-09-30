package ru.alexey.flowapp.convention

import com.android.build.api.dsl.CommonExtension
import org.gradle.api.JavaVersion
import org.gradle.api.Project
import org.gradle.api.artifacts.VersionCatalog
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.kotlin.dsl.getByType
import org.gradle.kotlin.dsl.withType
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinJvmCompile

/** `libs` version catalog for convention plugins. */
val Project.libs: VersionCatalog
    get() = extensions.getByType<VersionCatalogsExtension>().named("libs")

fun Project.version(name: String): String = libs.findVersion(name).get().requiredVersion

/**
 * Common Android + Kotlin setup: SDK, Java 17, compiler opt-ins.
 *
 * Since AGP 9 Kotlin is built into the Android plugins, so compiler options are set on
 * [KotlinJvmCompile] tasks directly.
 */
internal fun Project.configureKotlinAndroid(extension: CommonExtension) {
    extension.apply {
        compileSdk = version("android-compileSdk").toInt()
        defaultConfig.minSdk = version("android-minSdk").toInt()
        compileOptions.sourceCompatibility = JavaVersion.VERSION_17
        compileOptions.targetCompatibility = JavaVersion.VERSION_17
    }

    tasks.withType<KotlinJvmCompile>().configureEach {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_17)
            freeCompilerArgs.addAll(
                "-opt-in=kotlin.time.ExperimentalTime",
                "-XXLanguage:+ExplicitBackingFields",
            )
        }
    }
}
