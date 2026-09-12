plugins {
    id("com.yablonskyi.feature")
}

android {
    namespace = "com.yablonskyi.wizard"
    testOptions.unitTests.isIncludeAndroidResources = true
}
dependencies {
    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.robolectric)
    testImplementation(libs.androidx.ui.test.junit4)
}
