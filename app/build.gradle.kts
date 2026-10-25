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
        versionCode = 7
        versionName = "0.4.0"
    }
    // Release signing — two equivalent entry points:
    //   (1) Local:  RELEASE_SIGNING_JSON=/abs/path/to/signing.json
    //                { "storeFile": "/abs/path/keystore.jks",
    //                  "storePassword": "...", "keyPassword": "..." }
    //                keyAlias is fixed at "splitaudio" (matches existing release identity).
    //   (2) CI:     ANDROID_KEYSTORE_FILE=/tmp/keystore.jks
    //                ANDROID_KEYSTORE_PASSWORD=...
    //                ANDROID_KEY_ALIAS=splitaudio
    //                ANDROID_KEY_PASSWORD=...
    // When neither is set (e.g. PR build, dev build), the release variant is unsigned.
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
            isMinifyEnabled = true               // R8: code shrinking + obfuscation + optimization
            isShrinkResources = true             // resource shrinking
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            if (signingCfg != null) signingConfig = signingCfg
        }
        debug {
            // keep minify off for debug — fast iteration
        }
    }
    compileOptions { sourceCompatibility = JavaVersion.VERSION_17; targetCompatibility = JavaVersion.VERSION_17 }
    kotlinOptions { jvmTarget = "17" }
    buildFeatures {
        // No viewBinding, no Compose, no BuildConfig — keeps the APK minimal.
    }
    // R8 reports for verification (see docs/r8-optimization.md)
    androidResources {
        // no special config needed
    }
}
dependencies {
    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.appcompat:appcompat:1.7.0")  // AppCompatActivity base
    implementation("com.google.android.material:material:1.12.0")  // Material 3 components
    implementation("dev.rikka.shizuku:api:13.1.5")
    implementation("dev.rikka.shizuku:provider:13.1.5")
}
