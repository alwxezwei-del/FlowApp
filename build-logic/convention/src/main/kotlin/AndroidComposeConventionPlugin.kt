import com.android.build.api.dsl.CommonExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.getByType
import ru.alexey.flowapp.convention.configureAndroidCompose

/** Compose convention, applied on top of the library or application convention. */
class AndroidComposeConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            pluginManager.apply("org.jetbrains.kotlin.plugin.compose")

            configureAndroidCompose(extensions.getByType(CommonExtension::class))
        }
    }
}
