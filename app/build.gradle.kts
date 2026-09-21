plugins {
    id("com.yablonskyi.android.application")
    id("com.yablonskyi.android.hilt")
    alias(libs.plugins.kotlin.plugin.parcelize)
    id("com.github.skydoves.compose.stability.analyzer") version "0.7.2"
}

android {
    namespace = "com.yablonskyi.dndsheet"

    bundle {
        language {
            enableSplit = true
        }
    }
}

dependencies {
    // Project
    implementation(project(":feature:character"))
    implementation(project(":feature:character-spells"))
    implementation(project(":feature:compendium"))
    implementation(project(":feature:wizard"))
    implementation(project(":feature:settings"))
    implementation(project(":core:data"))
    implementation(project(":core:navigation"))
    implementation(project(":core:domain"))
    implementation(project(":core:model"))
    implementation(project(":core:ui"))
    implementation(project(":core:dice"))
    implementation(project(":core:pdf"))

    implementation(libs.androidx.material3.window.size.class1)
    implementation(libs.androidx.adaptive)
    implementation(libs.androidx.adaptive.layout)
    implementation(libs.androidx.adaptive.navigation)
    implementation(libs.androidx.material3.adaptive.navigation.suite)
    implementation(libs.androidx.material.icons.extended)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.core.splashscreen)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.work.runtime.ktx)
    implementation(libs.play.feature.delivery)
}
