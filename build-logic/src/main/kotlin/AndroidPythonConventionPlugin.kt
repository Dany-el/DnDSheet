import com.android.build.api.dsl.LibraryExtension
import com.chaquo.python.ChaquopyExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure

class AndroidPythonConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            pluginManager.apply("com.chaquo.python")

            extensions.configure<LibraryExtension> {
                defaultConfig {
                    ndk {
                        abiFilters += listOf("arm64-v8a", "x86_64")
                    }
                }
            }

            extensions.configure<ChaquopyExtension> {
                defaultConfig {
                    version = "3.13"
                    pip {
                        install("jinja2")
                    }
                }
            }
        }
    }
}