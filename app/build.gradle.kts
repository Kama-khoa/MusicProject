import com.android.build.api.dsl.AaptOptions

plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.example.music_project"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.music_project"
        minSdk = 24
        targetSdk = 34
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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

}

dependencies {
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation(libs.room.common)
    implementation(libs.room.runtime)
    implementation(libs.adapters)
    implementation(files("../spotify-app-remote-release-0.8.0.aar"))
    implementation(libs.recyclerview)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
    annotationProcessor(libs.room.compiler)
    implementation(libs.glide)
    annotationProcessor(libs.glide.compiler)
    implementation(libs.circleimageview)
    implementation(libs.retrofit.core)
    implementation(libs.retrofit.gson)
    implementation(libs.recyclerview)
    implementation(libs.androidx.media)
    implementation(kotlin("script-runtime"))
}