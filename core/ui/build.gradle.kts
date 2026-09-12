plugins {
    id("com.yablonskyi.android.library")
    id("com.yablonskyi.android.compose")
    id("com.yablonskyi.android.hilt")
}

android {
    namespace = "com.yablonskyi.ui"
    testOptions.unitTests.isIncludeAndroidResources = true
}

dependencies {
    // Project
    implementation(project(":core:model"))
    implementation(project(":core:domain"))

    implementation(libs.androidx.activity.compose)

    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.robolectric)
    testImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.test.manifest)
}