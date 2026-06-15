plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    id("kotlin-parcelize")
    id("com.google.gms.google-services")
    id("com.google.firebase.crashlytics")
}

android {
    namespace = "com.btb.beatthebox"
    compileSdk = 36
    defaultConfig {
        applicationId = "com.btb.beatthebox"
        minSdk = 26
        targetSdk = 36
        versionCode = 16
        versionName = "1.2.9"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
    signingConfigs {
        create("release") {
            val storeFilePath = project.property("RELEASE_STORE_FILE") as String
            val storePassword = project.property("RELEASE_STORE_PASSWORD") as String
            val keyAlias = project.property("RELEASE_KEY_ALIAS") as String
            val keyPassword = project.property("RELEASE_KEY_PASSWORD") as String

            storeFile = file(storeFilePath)
            this.storePassword = storePassword
            this.keyAlias = keyAlias
            this.keyPassword = keyPassword
        }
    }
    buildTypes {
        release {
            signingConfig = signingConfigs.getByName("release")
            isMinifyEnabled = true
            isShrinkResources = true
            isDebuggable = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
        debug {
            isMinifyEnabled = false
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

kotlin {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_11) // Or JVM_1_8, JVM_17, etc.
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.retrofit)
    implementation(libs.converter.gson)
    implementation(libs.androidx.viewpager2)
    implementation(libs.app.update.ktx)
    implementation(libs.firebase.crashlytics)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    implementation(libs.androidx.constraintlayout) // Or latest

    // Navigation Component
    implementation(libs.androidx.navigation.fragment.ktx) // Or latest
    implementation(libs.androidx.navigation.ui.ktx) // Or latest

    // ViewModel and LiveData (Lifecycle)
    implementation(libs.androidx.lifecycle.viewmodel.ktx) // Or latest
    implementation(libs.androidx.lifecycle.livedata.ktx) // Or latest
    implementation(libs.androidx.lifecycle.runtime.ktx) // For lifecycleScope

    // Retrofit & Gson (Networking)
    implementation(libs.logging.interceptor)
    implementation(libs.kotlinx.coroutines.core) // Or latest
    implementation(libs.kotlinx.coroutines.android) // Or latest

    // Glide (Image Loading for Carousel & potentially venue image)
    implementation(libs.glide) // Or latest
    // For Glide with Kotlin, you usually don't need a separate annotation processor
    // if you're using Kotlin exclusively. If you have Java mixed in or specific
    // Glide modules in Java, you might still need:
    // kapt("com.github.bumptech.glide:compiler:4.16.0")
    // Ensure you have the 'kotlin-kapt' plugin applied in your build.gradle if you use kapt.

    // ViewPager2 (for Carousel)
    implementation(libs.androidx.viewpager2) // Or latest version if available
    // Splash Screen API (Optional, for more control over animated splash)
    implementation(libs.androidx.core.splashscreen) // Or latest
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.analytics)
    implementation(libs.core)
    implementation(libs.androidx.credentials)
    implementation(libs.androidx.credentials.play.services.auth)
    implementation(libs.googleid)
    implementation(libs.androidx.datastore.preferences)
    implementation(libs.play.services.auth)
    implementation(libs.flexbox)
    implementation(libs.androidx.swiperefreshlayout)
    implementation(libs.shimmer)
    implementation(libs.api)
}
