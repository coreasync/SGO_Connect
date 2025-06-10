plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    kotlin("kapt") // добавьте kapt
    id("dagger.hilt.android.plugin") // добавьте плагин Hilt
}


android {
    namespace = "ru.niktoizniotkyda.netschooltokenapp"
    compileSdk = 35

    defaultConfig {
        applicationId = "ru.niktoizniotkyda.netschooltokenapp"
        minSdk = 27
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
    buildFeatures {
        viewBinding = true
    }
}

dependencies {
    kapt("com.google.dagger:hilt-android-compiler:2.56.2")

    implementation("javax.inject:javax.inject:1")
    implementation("com.google.code.gson:gson:2.13.1")

    implementation("androidx.datastore:datastore-core:1.1.7")
    implementation("androidx.datastore:datastore-preferences:1.1.7")
    implementation("androidx.datastore:datastore-preferences-core:1.1.7")

    implementation("com.google.dagger:dagger:2.56.2")
    implementation("com.google.dagger:hilt-core:2.56.2")
    implementation("com.google.dagger:hilt-android:2.56.2")

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}