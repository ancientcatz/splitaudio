plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}
android {
    namespace = "app.splitaudio"
    compileSdk = 34
    defaultConfig {
        applicationId = "app.splitaudio"
        minSdk = 29
        targetSdk = 34
        versionCode = 8
        versionName = "0.4.1"
    }
    // Release signing — two entry points:
    //   (1) Local:  RELEASE_SIGNING_JSON=/abs/path/to/signing.json
    //   (2) CI:     ANDROID_KEYSTORE_FILE=/tmp/keystore.jks + ANDROID_KEYSTORE_PASSWORD,
    //                ANDROID_KEY_ALIAS, ANDROID_KEY_PASSWORD
    // When neither is set, the release variant is unsigned.
    val localJson = System.getenv("RELEASE_SIGNING_JSON")?.let { file(it) }?.takeIf { it.exists() }
    val ciKs = System.getenv("ANDROID_KEYSTORE_FILE")?.takeIf { it.isNotEmpty() }?.let { file(it) }?.takeIf { it.exists() }
    val signingCfg = when {
        localJson != null -> {
            @Suppress("UNCHECKED_CAST")
            val sj = groovy.json.JsonSlurper().parse(localJson) as Map<String, Any?>
            signingConfigs.create("release") {
                storeFile = file(sj["storeFile"] as String)
                storePassword = sj["storePassword"] as String
                keyAlias = "splitaudio"
                keyPassword = sj["keyPassword"] as String
            }
        }
        ciKs != null -> {
            signingConfigs.create("release") {
                storeFile = ciKs
                storePassword = System.getenv("ANDROID_KEYSTORE_PASSWORD") ?: ""
                keyAlias = System.getenv("ANDROID_KEY_ALIAS") ?: "splitaudio"
                keyPassword = System.getenv("ANDROID_KEY_PASSWORD") ?: ""
            }
        }
        else -> null
    }
    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            if (signingCfg != null) signingConfig = signingCfg
        }
        debug { }
    }
    compileOptions { sourceCompatibility = JavaVersion.VERSION_17; targetCompatibility = JavaVersion.VERSION_17 }
    kotlinOptions { jvmTarget = "17" }
    buildFeatures {
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.14"
    }
}
dependencies {
    // AndroidX core + Activity Compose (for setContent + enableEdgeToEdge)
    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.activity:activity-compose:1.9.3")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.8.6")

    // Jetpack Compose + Material 3
    implementation(platform("androidx.compose:compose-bom:2024.09.03"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")
    debugImplementation("androidx.compose.ui:ui-tooling")

    // Material Components — kept for the XML theme parent (Theme.Material3.DayNight).
    implementation("com.google.android.material:material:1.12.0")

    // Shizuku
    implementation("dev.rikka.shizuku:api:13.1.5")
    implementation("dev.rikka.shizuku:provider:13.1.5")
}
