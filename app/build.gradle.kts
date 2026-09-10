plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
}

android {
    namespace = "com.lq666.statusbarcleaner"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.lq666.statusbarcleaner"
        minSdk = 31
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            // 用 debug 签名(自用)
            signingConfig = signingConfigs.getByName("debug")
        }
        debug {
            // 默认 debug 签名
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }
}

dependencies {
    // LSPosed / Xposed API
    compileOnly(libs.libxposed.api)
}