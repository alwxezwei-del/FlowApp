import com.android.build.api.dsl.ApplicationExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import ru.alexey.flowapp.convention.configureKotlinAndroid
import ru.alexey.flowapp.convention.version

/** Convention for the single `com.android.application` module. */
class AndroidApplicationConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            pluginManager.apply("com.android.application")

            extensions.configure<ApplicationExtension> {
                configureKotlinAndroid(this)
                defaultConfig.targetSdk = version("android-targetSdk").toInt()
            }
        }
    }
}
