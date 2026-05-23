plugins {
    alias(libs.plugins.android.application)
}

buildscript {
    dependencies {
        classpath("androidx.navigation:navigation-safe-args-gradle-plugin:2.7.7")
    }
}

android {
    namespace = "com.cyberpath.smartlearn"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.cyberpath.smartlearn"
        minSdk = 24
        targetSdk = 36
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

    buildFeatures {
        viewBinding = true
    }
    sourceSets {
        getByName("main") {
            assets {
                srcDirs("src\\main\\assets", "src\\main\\assets")
            }
        }
    }
}

dependencies {
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.constraintlayout)
    implementation(libs.media3.ui)
    implementation(libs.media3.exoplayer)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)

    implementation("androidx.biometric:biometric:1.1.0")

    implementation("androidx.navigation:navigation-fragment:2.9.5")
    implementation("androidx.navigation:navigation-ui:2.9.5")
    implementation("androidx.activity:activity-ktx:1.8.2")
    implementation("androidx.activity:activity:1.8.2")

    implementation ("androidx.appcompat:appcompat:1.7.0")
    implementation ("com.google.android.material:material:1.12.0")
    implementation ("androidx.recyclerview:recyclerview:1.3.2")

    implementation ("com.squareup.retrofit2:retrofit:2.9.0")
    implementation ("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation ("androidx.lifecycle:lifecycle-runtime-ktx:2.8.6")

    implementation ("org.tensorflow:tensorflow-lite:2.14.0")
    implementation ("com.google.mediapipe:tasks-audio:latest.release")

    implementation ("com.squareup.okhttp3:logging-interceptor:4.9.3")

    compileOnly("org.projectlombok:lombok:1.18.42")
    annotationProcessor("org.projectlombok:lombok:1.18.42")
    testCompileOnly("org.projectlombok:lombok:1.18.42")
    testAnnotationProcessor("org.projectlombok:lombok:1.18.42")

    compileOnly("org.projectlombok:lombok:1.18.34")
    annotationProcessor("org.projectlombok:lombok:1.18.34")

    implementation("com.google.code.gson:gson:2.10.1")
    implementation("com.github.bumptech.glide:glide:4.15.1")
    annotationProcessor("com.github.bumptech.glide:compiler:4.15.1")
}

apply(plugin = "androidx.navigation.safeargs")

