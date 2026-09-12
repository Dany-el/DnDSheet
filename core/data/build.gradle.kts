plugins {
    id("com.yablonskyi.android.base")
    id("com.yablonskyi.android.room")
    id("com.yablonskyi.android.hilt")
}

android {
    namespace = "com.yablonskyi.data"
}

dependencies {
    implementation(project(":core:domain"))
    implementation(project(":core:model"))
    implementation(libs.okhttp3)
    implementation(libs.androidx.core.ktx)
}