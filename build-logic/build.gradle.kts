import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.`kotlin-dsl`

plugins {
    `kotlin-dsl`
}

kotlin {
    jvmToolchain(17)
}

dependencies {
    implementation(libs.gradle)
    implementation(libs.kotlin.gradle.plugin)
    implementation(libs.kotlin.composeCompiler.gradlePlugin)
    implementation(libs.chaquopy.gradle)
}

gradlePlugin {
    plugins {
        // Android Application
        register("androidApplication") {
            id = "com.yablonskyi.android.application"
            implementationClass = "AndroidApplicationConventionPlugin"
        }
        // Android Library
        register("androidLibrary") {
            id = "com.yablonskyi.android.library"
            implementationClass = "AndroidLibraryConventionPlugin"
        }
        // Compose
        register("composeDependencies") {
            id = "com.yablonskyi.android.compose"
            implementationClass = "AndroidComposeConventionPlugin"
        }
        // Hilt
        register("hiltDependencies") {
            id = "com.yablonskyi.android.hilt"
            implementationClass = "AndroidHiltConventionPlugin"
        }
        // Room
        register("roomDependencies") {
            id = "com.yablonskyi.android.room"
            implementationClass = "AndroidRoomConventionPlugin"
        }
        // Other/Base
        register("baseDependencies") {
            id = "com.yablonskyi.android.base"
            implementationClass = "AndroidBaseConventionPlugin"
        }
        // Python
        register("pythonSupport") {
            id = "com.yablonskyi.android.python"
            implementationClass = "AndroidPythonConventionPlugin"
        }

        // Modules
        // Feature Module
        register("featureModule") {
            id = "com.yablonskyi.feature"
            implementationClass = "AndroidFeatureConventionPlugin"
        }
    }
}