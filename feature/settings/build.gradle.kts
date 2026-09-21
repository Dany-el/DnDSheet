plugins {
    id("com.yablonskyi.feature")
}

android {
    namespace = "com.yablonskyi.settings"
}

dependencies {
    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
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
