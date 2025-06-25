plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.google.gms.google.services)
}

android {
    namespace = "com.example.adminwavesoffood"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.adminwavesoffood"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildFeatures {
        viewBinding = true
    }

    buildTypes {
        release {
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

    kotlinOptions {
        jvmTarget = "11"
    }
}

dependencies {
    // AndroidX & Material
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)

    implementation ("com.cloudinary:cloudinary-android:2.3.1")


    // Firebase Auth
    implementation("com.google.firebase:firebase-auth:22.3.1")

    // Firebase Realtime Database
    implementation("com.google.firebase:firebase-database:20.3.1")

    // Google Sign-In
    implementation("com.google.android.gms:play-services-auth:21.0.0")
    implementation(libs.firebase.storage)
    implementation ("com.github.bumptech.glide:glide:4.16.0")
    implementation(libs.firebase.messaging)

    // Optional: Credential API - REMOVED due to preview SDK requirement
    // implementation("androidx.credentials:credentials:1.2.0-alpha02")
    // implementation("androidx.credentials:credentials-play-services-auth:1.2.0-alpha02")

    // Testing
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}
