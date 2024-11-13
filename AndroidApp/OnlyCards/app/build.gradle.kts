plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    id("com.google.devtools.ksp") version "1.9.0-1.0.13"
    id("org.jetbrains.kotlin.kapt")
}

android {
    namespace = "unical.enterpriceapplication.onlycards"
    compileSdk = 34

    defaultConfig {
        applicationId = "unical.enterpriceapplication.onlycards"
        minSdk = 30
        targetSdk = 34
        versionCode = 1
        versionName = "1.0.0"
        addManifestPlaceholders(
            mapOf(
                "appAuthRedirectScheme" to "unical.enterpriceapplication.onlycards"
            )
        )

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
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
        compose = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.15"
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

configurations.all {
    resolutionStrategy {
        force("androidx.test.espresso:espresso-core:3.5.0")
    }
}

dependencies {
    // Implementazioni principali
    implementation(libs.androidx.activity.ktx)
    implementation(libs.appauth)
    implementation(libs.androidx.ui.text.google.fonts)
    implementation(libs.androidx.ui.test.android)
    implementation(libs.coil.compose)
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.compose.material)
    implementation(libs.androidx.room.ktx)
    implementation(libs.androidx.material3.v110alpha06)
    implementation(libs.espresso.core)
    implementation(libs.androidx.constraintlayout.compose)
    implementation(libs.androidx.ui.text)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.material.icons.core)
    implementation(libs.androidx.material.icons.extended)
    implementation(libs.androidx.browser)
    implementation(libs.gson)
    implementation(libs.accompanist.pager.vversion)
    implementation(libs.accompanist.pager.indicators)
    implementation(libs.firebase.firestore.ktx)
    implementation(libs.okhttp)
    implementation(libs.composable.graphs)
    implementation(libs.slugify)
    implementation(libs.kotlinx.serialization.json)


    // Room
    ksp(libs.androidx.room.compiler)
    annotationProcessor(libs.androidx.room.compiler)

    // Dipendenze di Test
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)

    // Debug e strumenti
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)

}