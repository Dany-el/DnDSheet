plugins {
    id("com.yablonskyi.android.library")
}

android {
    namespace = "com.yablonskyi.domain"
}

dependencies {
    implementation(project(":core:model"))
    implementation(libs.androidx.annotation)
    implementation(libs.kotlinx.coroutines)
}
