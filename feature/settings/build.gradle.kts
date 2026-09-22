plugins {
    id("com.yablonskyi.feature")
}

android {
    namespace = "com.yablonskyi.settings"
    testOptions.unitTests.isIncludeAndroidResources = true
}

dependencies {
    implementation(libs.androidx.activity.compose)
    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.robolectric)
    testImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.test.manifest)
    // For user authentication
    implementation(libs.play.services.auth)
    // For the Google Drive API
    implementation(libs.google.api.client.android)
    implementation(libs.google.api.services.drive)
    implementation(libs.androidx.credentials)
    implementation(libs.androidx.credentials.play.services.auth)
    implementation(libs.google.http.client.gson)
    // DataStore
    implementation(libs.androidx.datastore.preferences)
}
