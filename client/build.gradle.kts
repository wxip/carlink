@Suppress("DSL_SCOPE_VIOLATION") // TODO: Remove once KTIJ-19369 is fixed
plugins {
    alias(libs.plugins.androidApplication)
}

android {
    namespace = "top.wxip.carlink.client"
    compileSdk = 34

    defaultConfig {
        applicationId = "top.wxip.carlink.client"
        minSdk = 28
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
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
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

dependencies {
    implementation(libs.logger)
    implementation(libs.adblib)
    implementation(libs.hutool.all)
    implementation(project(":common"))
    compileOnly(libs.lombok)
    annotationProcessor(libs.lombok)
}