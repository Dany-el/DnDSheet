plugins {
    id("com.yablonskyi.android.base")
    id("com.yablonskyi.android.room")
    id("com.yablonskyi.android.hilt")
}

android {
    namespace = "com.yablonskyi.data"
    testOptions.unitTests.isIncludeAndroidResources = true
}

dependencies {
    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.robolectric)
    testImplementation(libs.androidx.test.core)

    implementation(project(":core:domain"))
    implementation(project(":core:model"))
    implementation(libs.okhttp3)
    implementation(libs.androidx.core.ktx)
    implementation(libs.kotlinx.serialization.json)
}
