plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    // Temporarily disable Hilt to fix build issues
    // alias(libs.plugins.hilt)
    // id("kotlin-kapt")
    // Temporarily disable Firebase to fix build issues
    // alias(libs.plugins.google.services)
    // alias(libs.plugins.firebase.crashlytics)
}

android {
    namespace = "com.example.autostradaauctions"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.autostradaauctions"
        minSdk = 29
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        debug {
            isDebuggable = true
            applicationIdSuffix = ".debug"
            versionNameSuffix = "-debug"
            
            buildConfigField("String", "BASE_URL", "\"http://10.0.2.2:5117/api/\"")
            buildConfigField("String", "SIGNALR_HUB_URL", "\"http://10.0.2.2:5117/biddingHub\"")
            buildConfigField("boolean", "ENABLE_LOGGING", "true")
        }
        
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            isDebuggable = false
            
            // Production API endpoints (replace with your production URLs)
            buildConfigField("String", "BASE_URL", "\"https://your-production-api.com/api/\"")
            buildConfigField("String", "SIGNALR_HUB_URL", "\"https://your-production-api.com/biddingHub\"")
            buildConfigField("boolean", "ENABLE_LOGGING", "false")
            
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            
            // Enable signing for release builds
            signingConfig = signingConfigs.getByName("debug") // Replace with release signing config
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.14"
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.savedstate)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)

    // Temporarily disable Hilt and Room to fix build issues
    // implementation(libs.hilt.android)
    // kapt(libs.hilt.compiler)
    // implementation(libs.hilt.navigation.compose)
    // implementation(libs.room.runtime)
    // kapt(libs.room.compiler)
    // implementation(libs.room.ktx)

    // Retrofit & OkHttp
    implementation(libs.retrofit)
    implementation(libs.retrofit.gson)
    implementation(libs.okhttp)
    implementation(libs.okhttp.logging)

    // Navigation
    implementation(libs.navigation.compose)

    // Lifecycle
    implementation(libs.lifecycle.viewmodel.compose)
    implementation(libs.lifecycle.runtime.compose)

    // Coil
    implementation(libs.coil.compose)

    // Coroutines
    implementation(libs.kotlinx.coroutines.android)

    // Material Icons
    implementation(libs.material.icons.extended)

    // SignalR for real-time communication
    implementation("com.microsoft.signalr:signalr:7.0.0")

    // Security for encrypted preferences
    implementation("androidx.security:security-crypto:1.1.0-alpha06")

    // Testing dependencies
    testImplementation(libs.junit)
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.7.3")
    testImplementation("org.mockito:mockito-core:5.5.0")
    testImplementation("org.mockito.kotlin:mockito-kotlin:5.1.0")
    testImplementation("androidx.arch.core:core-testing:2.2.0")
    testImplementation("com.squareup.okhttp3:mockwebserver:4.11.0")
    
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    androidTestImplementation("androidx.compose.ui:ui-test-manifest")
    androidTestImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.7.3")
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}