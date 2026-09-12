plugins {
    id("com.yablonskyi.android.library")
    alias(libs.plugins.serialization)
}

android {
    namespace = "com.yablonskyi.model"
}

dependencies {
    implementation(libs.androidx.annotation)
    implementation(libs.kotlinx.serialization.json)
}
