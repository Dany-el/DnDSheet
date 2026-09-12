import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies

class AndroidFeatureConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            pluginManager.apply("com.yablonskyi.android.library")
            pluginManager.apply("com.yablonskyi.android.compose")
            pluginManager.apply("com.yablonskyi.android.hilt")
            pluginManager.apply("com.yablonskyi.android.base")

            dependencies {
                add("implementation", target.project(":core:ui"))
                add("implementation", target.project(":core:data"))
                add("implementation", target.project(":core:model"))
                add("implementation", target.project(":core:domain"))
                add("implementation", target.project(":core:navigation"))
            }
        }
    }
}