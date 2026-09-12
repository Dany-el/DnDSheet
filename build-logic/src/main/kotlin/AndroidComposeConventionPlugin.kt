import com.android.build.api.dsl.LibraryExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.getByType
import org.jetbrains.kotlin.compose.compiler.gradle.ComposeCompilerGradlePluginExtension

class AndroidComposeConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            pluginManager.apply("com.android.library")
            pluginManager.apply("org.jetbrains.kotlin.plugin.compose")

            extensions.configure<LibraryExtension> {
                buildFeatures {
                    compose = true
                }
            }

            extensions.configure<ComposeCompilerGradlePluginExtension> {
                stabilityConfigurationFiles.add(
                    rootProject.layout.projectDirectory.file("compose_compiler_config.conf")
                )
            }

            val libs = extensions.getByType<VersionCatalogsExtension>().named("libs")

            dependencies {
                add("implementation", libs.findLibrary("androidx-compose-foundation").get())
                add("implementation", libs.findLibrary("androidx-compose-foundation-layout").get())
                add("implementation", libs.findLibrary("androidx-compose-material3").get())
                add("implementation", libs.findLibrary("androidx-activity-ktx").get())
                add("implementation", libs.findLibrary("androidx-runtime").get())
                add("implementation", libs.findLibrary("androidx-core-ktx").get())
                add("implementation", libs.findLibrary("androidx-material3-window-size-class1").get())
                add("implementation", libs.findLibrary("androidx-adaptive").get())
                add("implementation", libs.findLibrary("androidx-ui-tooling-preview").get())
                add("debugImplementation", libs.findLibrary("androidx-ui-tooling").get())
                add("debugImplementation", libs.findLibrary("androidx-ui-test-manifest").get())
                add("implementation", libs.findLibrary("androidx-material3-adaptive-navigation-suite").get())
                add("implementation", libs.findLibrary("androidx-lifecycle-viewModelCompose").get())
                add("implementation", libs.findLibrary("androidx-compose-ui-text-google-fonts").get())
                add("implementation", libs.findLibrary("androidx-material-icons-extended").get())
                add("implementation", libs.findLibrary("androidx-appcompat").get())
                add("implementation", libs.findLibrary("androidx-compose-animation").get())
                add("implementation", libs.findLibrary("androidx-lifecycle-runtime-ktx").get())
            }
        }
    }
}
