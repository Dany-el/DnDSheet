plugins {
    id("com.yablonskyi.feature")
}

android {
    namespace = "com.yablonskyi.character"
    testOptions.unitTests.isIncludeAndroidResources = true
}

dependencies {
    implementation(libs.reorderable)
    implementation(project(":core:dice"))
    implementation(libs.coil.compose)
    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.robolectric)
    testImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.test.manifest)
}
