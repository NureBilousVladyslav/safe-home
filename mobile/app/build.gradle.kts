import com.android.build.api.dsl.ApplicationExtension

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.hilt)
    alias(libs.plugins.ksp)
    alias(libs.plugins.google.plugin)
}

extensions.configure<ApplicationExtension>("android") {
    namespace = "com.example.safehome"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.safehome"
        minSdk = 24
        targetSdk = 37
        versionCode = 1
        versionName = "1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    signingConfigs {
        create("release") {
            storeFile = file("../release.jks")
            storePassword = System.getenv("KEYSTORE_PASSWORD") ?: ""
            keyAlias = System.getenv("KEY_ALIAS") ?: ""
            keyPassword = System.getenv("KEY_PASSWORD") ?: ""
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
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildFeatures {
        viewBinding = true
        compose = true
    }

    androidResources {
        noCompress += "tflite"
    }
}

dependencies {
    // AndroidX core / UI
    implementation(libs.androidx.activity.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.browser)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.core.splashscreen)
    implementation(libs.androidx.fragment.ktx)
    implementation(libs.material)

    // Compose
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)

    // Lifecycle / Navigation / Background
    implementation(libs.androidx.lifecycle.livedata.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)
    implementation(libs.androidx.worker)

    // Data / Network / Security
    implementation(libs.androidx.room.ktx)
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.security.crypto)
    implementation(libs.converter.gson)
    implementation(libs.datastore.preferences)
    implementation(libs.jakewharton.timber)
    implementation(libs.logging.interceptor)
    implementation(libs.retrofit2.retrofit)

    // Firebase / Google services
    implementation(platform(libs.google.firebase.bom))
    implementation(libs.firebase.auth)
    implementation(libs.firebase.messaging)
    implementation(libs.google.firebase)
    implementation(libs.play.services.auth)

    // Camera / ML
    implementation(libs.androidx.camera.camera2)
    implementation(libs.androidx.camera.core)
    implementation(libs.androidx.camera.lifecycle)
    implementation(libs.androidx.camera.view)
    implementation(libs.litert)
    implementation(libs.play.services.mlkit.face.detection)
    implementation(libs.vision.common)

    // Dependency injection
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)

    // Annotation processors
    ksp(libs.androidx.room.compiler)

    // Tests
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)

    // Debug
    debugImplementation(libs.androidx.ui.test.manifest)
    debugImplementation(libs.androidx.ui.tooling)
}
