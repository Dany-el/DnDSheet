plugins {
    id("com.yablonskyi.android.library")
    id("com.yablonskyi.android.compose")
    id("com.yablonskyi.android.hilt")
}

android {
    namespace = "com.yablonskyi.dice"
}

dependencies {
    implementation(project(":core:domain"))
    implementation(project(":core:model"))
    implementation(project(":core:ui"))
    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
}
