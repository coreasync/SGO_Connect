plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    kotlin("kapt")
    id("dagger.hilt.android.plugin")
    id("com.google.protobuf") version "0.9.4"
}

protobuf {
    protoc {
        artifact = "com.google.protobuf:protoc:4.31.1"
    }
    generateProtoTasks {
        all().forEach { task ->
            task.builtins {
                create("java") {
                    option("lite")
                }
                create("kotlin") {
                    option("lite")
                }
            }
        }
    }
}

android {
    namespace = "ru.niktoizniotkyda.netschooltokenapp"
    //noinspection GradleDependency
    compileSdk = 35

    defaultConfig {
        applicationId = "ru.niktoizniotkyda.netschooltokenapp"
        minSdk = 26
        //noinspection OldTargetApi
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
    implementation("com.google.protobuf:protobuf-kotlin-lite:3.24.0")
    implementation("com.google.protobuf:protobuf-javalite:3.24.0")

    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("com.squareup.okhttp3:logging-interceptor:5.0.0-alpha.10")

    implementation("androidx.fragment:fragment-ktx:1.6.2")
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)

    kapt("com.google.dagger:hilt-android-compiler:2.56.2")

    implementation("javax.inject:javax.inject:1")
    implementation("com.google.code.gson:gson:2.13.1")

    implementation("androidx.datastore:datastore-core:1.1.7")
    implementation("com.google.protobuf:protobuf-javalite:4.31.1")
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