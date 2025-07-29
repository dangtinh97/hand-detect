plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
}

android {
    namespace = "com.vudangtinh.handdetection"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.vudangtinh.handdetection"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
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

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)



    implementation ("androidx.camera:camera-core:1.2.0-alpha02")

    // CameraX Camera2 extensions
    implementation ("androidx.camera:camera-camera2:1.2.0-alpha02")

    // CameraX Lifecycle library
    implementation ("androidx.camera:camera-lifecycle:1.2.0-alpha02")

    // CameraX View class
    implementation ("androidx.camera:camera-view:1.2.0-alpha02")

    // WindowManager
    implementation ("androidx.window:window:1.1.0-alpha03")
    // MediaPipe Library
    implementation ("com.google.mediapipe:tasks-vision:0.10.26")
}