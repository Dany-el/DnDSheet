plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.ksp)
    alias(libs.plugins.hilt)
    alias(libs.plugins.serialization)
    alias(libs.plugins.kotlin.plugin.parcelize)
    id("com.github.skydoves.compose.stability.analyzer") version "0.7.2"
}

android {
    namespace = "com.yablonskyi.dndsheet"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.yablonskyi.dndsheet"
        minSdk = 28
        targetSdk = 36
        versionCode = 3
        versionName = "0.1.2-beta"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildFeatures {
        buildConfig = true
    }

    signingConfigs {
        create("release") {
            storeFile = file("C:\\Users\\Daniel\\Downloads\\dnd-sheet-release-key.jks")
            storePassword = "regner13242005"
            keyAlias = "dnd_sheet_app"
            keyPassword = "regner13242005"
        }
    }

    buildTypes {
        release {
            signingConfig = signingConfigs.getByName("release")
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        compose = true
    }

    packaging {
        resources {
            excludes += "META-INF/DEPENDENCIES"
        }
    }
}

dependencies {

    implementation(libs.androidx.compose.foundation)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.exifinterface)
    implementation(libs.androidx.compose.animation)
    // Room
    ksp(libs.androidx.room.compiler)
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    testImplementation(libs.androidx.room.testing)

    // Hilt
    implementation(libs.hilt)
    implementation(libs.androidx.hilt.navigation.compose)
    implementation(libs.androidx.hilt.work)
    ksp(libs.hilt.compiler)

    // Jetpack Compose
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.lifecycle.viewModelCompose)
    implementation(libs.androidx.compose.runtime)
    implementation(libs.androidx.material.icons.extended)
    implementation(libs.androidx.appcompat)

    implementation(libs.androidx.datastore.preferences)
    implementation(libs.androidx.core.splashscreen)

    implementation(libs.gson)

    implementation(libs.coil.compose)
    implementation(libs.androidx.activity.ktx)
    implementation(libs.kotlinx.serialization.json)

    implementation(libs.itextpdf.kernel)
    implementation(libs.itextpdf.io)
    implementation(libs.itextpdf.layout)
    implementation(libs.itextpdf.forms)

    implementation(libs.androidx.material3.window.size.class1)
    implementation(libs.androidx.adaptive)
    implementation(libs.androidx.adaptive.layout)
    implementation(libs.androidx.adaptive.navigation)
    implementation(libs.androidx.material3.adaptive.navigation.suite)

    // For user authentication
    implementation(libs.play.services.auth)

    // Workers
    implementation(libs.androidx.work.runtime.ktx)

    // For the Google Drive API
    implementation(libs.google.api.client.android)
    implementation(libs.google.api.services.drive)
    implementation(libs.androidx.credentials)
    implementation(libs.androidx.credentials.play.services.auth)
    implementation(libs.google.http.client.gson)

    implementation(libs.androidx.compose.ui.text.google.fonts)

    // Test
    testImplementation(libs.kotlinx.coroutines.test)

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
    testImplementation(kotlin("test"))
}