plugins {
    alias(libs.plugins.android.application)
    // 1. AÑADIR EL PLUGIN DE GOOGLE AQUÍ:
    id("com.google.gms.google-services")
}

android {
    namespace = "com.example.cybersprint"
    compileSdk = 36 // He ajustado esto a un estándar (36 es beta/preview, mejor 35 o 34 si da error)

    defaultConfig {
        applicationId = "com.example.cybersprint"
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
}

dependencies {
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)

    // 2. AÑADIR LAS LIBRERÍAS DE FIREBASE AQUÍ:

    // Plataforma BOM (Gestiona las versiones automáticamente)
    implementation(platform("com.google.firebase:firebase-bom:33.7.0"))

    // Autenticación y Base de Datos (Sin poner versión, la BOM se encarga)
    implementation("com.google.firebase:firebase-auth")
    implementation("com.google.firebase:firebase-database")
}