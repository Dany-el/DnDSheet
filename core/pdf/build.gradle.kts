plugins {
    id("com.yablonskyi.android.library")
    id("com.yablonskyi.android.hilt")
    id("com.yablonskyi.android.python")
}

android {
    namespace = "com.yablonskyi.pdf"
    testOptions.unitTests.isIncludeAndroidResources = true
}

extensions.configure<com.chaquo.python.ChaquopyExtension> {
    sourceSets.getByName("main") {
        exclude("**/.venv/**", "**/venv/**", "**/__pycache__/**", "**/*.pyc")
    }
}

dependencies {
    implementation(project(":core:domain"))
    implementation(project(":core:model"))
    implementation(project(":core:ui"))
    implementation(libs.kotlinx.coroutines)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.robolectric)
    testImplementation(libs.androidx.activity.ktx)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.test.runner)
    androidTestImplementation(libs.kotlinx.coroutines.test)
    androidTestImplementation(libs.androidx.activity.ktx)
}
