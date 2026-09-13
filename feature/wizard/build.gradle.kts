plugins {
    id("com.yablonskyi.feature")
}

android {
    namespace = "com.yablonskyi.wizard"
    testOptions.unitTests.isIncludeAndroidResources = true
}
extensions.configure<com.android.build.api.dsl.LibraryExtension> {
    sourceSets.getByName("test").kotlin.srcDir("src/composeTest/java")
    sourceSets.getByName("androidTest").kotlin.srcDir("src/composeTest/java")
}

dependencies {
    androidTestImplementation(libs.androidx.ui.test.junit4)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.test.runner)
    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.robolectric)
    testImplementation(libs.androidx.ui.test.junit4)
}
