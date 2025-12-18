plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    id("kotlin-kapt")
    id("org.jetbrains.kotlin.plugin.serialization") version "1.9.0"
}

android {
    namespace = "com.example.todolist"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.todolist"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        // BuildConfig fields for optional remote endpoints and API keys (empty by default)
        buildConfigField("String", "TASK_BASE_URL", "\"\"")
        buildConfigField("String", "TASK_API_KEY", "\"\"")
        buildConfigField("String", "AI_BASE_URL", "\"\"")
        buildConfigField("String", "AI_API_KEY", "\"\"")
        buildConfigField("String", "GEMINI_API_KEY", "\"AIzaSyBxlkwynx0EzxG55SLjqjtcEo-TuVzEEvg\"")
        buildConfigField("String", "GOOGLE_WEB_CLIENT_ID", "\"821871972466-31dinbckig324gchs57r8a9n9lqm6nsu.apps.googleusercontent.com\"")
    }

    testOptions {
        unitTests.isIncludeAndroidResources = true
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = signingConfigs.getByName("debug")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
        // Enable desugaring for java.time on older Android devices (minSdk < 26)
        isCoreLibraryDesugaringEnabled = true
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
        // Enable generation of BuildConfig fields declared in defaultConfig
        buildConfig = true
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
            excludes += "/META-INF/INDEX.LIST"
            excludes += "/META-INF/DEPENDENCIES"
            excludes += "/META-INF/LICENSE.md"
            excludes += "/META-INF/NOTICE.md"
        }
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    // Material extended icons (managed by Compose BOM)
    implementation(libs.androidx.compose.material.material.icons.extended13)
    // Google Fonts for Jetpack Compose
    implementation(libs.androidx.compose.ui.ui.text.google.fonts17)
    // Navigation for Compose
    implementation(libs.androidx.navigation.navigation.compose12)
    // Coil for image loading in Compose
    implementation(libs.io.coil.kt.coil.compose13)
    // Network: Retrofit + Gson + OkHttp
    implementation(libs.com.squareup.retrofit2.retrofit10)
    implementation(libs.com.squareup.retrofit2.converter.gson9)
    implementation(libs.com.squareup.okhttp3.logging.interceptor8)
    // Room
    implementation(libs.androidx.room.room.ktx8)
    implementation(libs.androidx.room.room.runtime21)
    kapt("androidx.room:room-compiler:2.8.4")
    // Desugaring libs for java.time on older devices
    coreLibraryDesugaring(libs.com.android.tools.desugar.jdk.libs6)
    // WorkManager for notifications
    implementation(libs.androidx.work.work.runtime.ktx6)

    // Gemini AI
    implementation(libs.google.generativeai)

    // PyTorch Mobile for ML inference (Translation model - 11M params)
    implementation(libs.pytorch.android)
    implementation(libs.pytorch.pytorch.android.torchvision)

    // JSON serialization
    implementation(libs.org.jetbrains.kotlinx.kotlinx.serialization.json5)

    // Accompanist Permissions
    implementation(libs.com.google.accompanist.accompanist.permissions)

    // Google Sign-In (Credential Manager)
    implementation(libs.androidx.credentials.credentials2)
    implementation(libs.androidx.credentials.credentials.play.services.auth28)
    implementation(libs.com.google.android.libraries.identity.googleid.googleid3)

    // Google Play Services Auth (for OAuth with scopes)
    implementation(libs.com.google.android.gms.play.services.auth16)

    // Coroutines Play Services (for await() on Tasks)
    implementation(libs.org.jetbrains.kotlinx.kotlinx.coroutines.play.services4)

    // Google API Client for Calendar
    implementation("com.google.api-client:google-api-client-android:2.2.0") {
        exclude(group = "org.apache.httpcomponents")
    }
    implementation("com.google.apis:google-api-services-calendar:v3-rev20231123-2.0.0") {
        exclude(group = "org.apache.httpcomponents")
    }
    implementation("com.google.auth:google-auth-library-oauth2-http:1.23.0")
    implementation("com.google.android.gms:play-services-auth:21.2.0")

    // Google Calendar API
    implementation("com.google.api-client:google-api-client-android:2.2.0")
    implementation("com.google.apis:google-api-services-calendar:v3-rev20220715-2.0.0")


    // Test dependencies
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.10.2")
    testImplementation("androidx.arch.core:core-testing:2.2.0")
    testImplementation("io.mockk:mockk:1.14.6")
    testImplementation("org.robolectric:robolectric:4.16")
    testImplementation("androidx.test:core:1.7.0")

    // Instrumented test dependencies
    androidTestImplementation("androidx.test:core-ktx:1.7.0")
    androidTestImplementation("androidx.test.ext:junit:1.3.0")
    androidTestImplementation("androidx.test:runner:1.7.0")
    androidTestImplementation("androidx.test:rules:1.7.0")

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}