import com.android.build.api.dsl.ApplicationExtension
import org.gradle.api.JavaVersion
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.getByType
import java.util.Properties

class AndroidApplicationConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            pluginManager.apply("com.android.application")
            pluginManager.apply("org.jetbrains.kotlin.plugin.compose")

            val libs = extensions.getByType<VersionCatalogsExtension>().named("libs")
            val versionProps = Properties().apply {
                load(rootProject.file("version.properties").inputStream())
            }

            extensions.configure<ApplicationExtension> {
                compileSdk = libs.findVersion("compileSdk").get().requiredVersion.toInt()

                defaultConfig {
                    applicationId = "com.yablonskyi.dndsheet"

                    minSdk = libs.findVersion("minSdk").get().requiredVersion.toInt()
                    targetSdk = libs.findVersion("targetSdk").get().requiredVersion.toInt()

                    versionCode = versionProps["VERSION_CODE"].toString().toInt()
                    versionName = versionProps["VERSION_NAME"].toString()

                    testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
                }

                packaging {
                    resources {
                        excludes += "META-INF/DEPENDENCIES"
                    }
                }

                compileOptions {
                    sourceCompatibility = JavaVersion.VERSION_17
                    targetCompatibility = JavaVersion.VERSION_17
                }
            }
        }
    }
}