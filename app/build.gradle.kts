plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.kotlin.android)
}

android {
    namespace = "com.ams.propertybhandar"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.ams.propertybhandar"
        minSdk = 26
        targetSdk = 34
        versionCode = 8
        versionName = "1.7"

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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    buildFeatures {
        viewBinding = true
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.room.ktx)
    implementation(libs.volley)
    implementation(libs.androidx.core.splashscreen)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    implementation(libs.okhttp)
    implementation(libs.logging.interceptor)
    implementation (libs.gson)
    implementation (libs.circleimageview)
    implementation(libs.glide)
    annotationProcessor(libs.compiler)
    implementation(libs.androidx.core.splashscreen)
    implementation(libs.picasso)
    implementation(libs.picasso.v28)
    implementation(libs.mpandroidchart)
    implementation(libs.androidx.multidex)
    implementation(libs.lottie)
    implementation(libs.poi)
    implementation(libs.poi.ooxml)
}