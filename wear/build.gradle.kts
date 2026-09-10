import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlinx.serialization)
    alias(libs.plugins.kotlin.compose)
}

// Load signing properties from local.properties (never committed to git)
val localProps = Properties().apply {
    val f = rootProject.file("local.properties")
    if (f.exists()) load(f.reader())
}

android {
    namespace = "com.philornot.slownikjezykatrudnego.wear"
    compileSdk = 37

    defaultConfig {
        applicationId = "com.philornot.slownikjezykatrudnego"
        minSdk = 30
        targetSdk = 37
        versionCode = 40
        versionName = "1.8.3"

        vectorDrawables {
            useSupportLibrary = true
        }
    }

    signingConfigs {
        create("release") {
            if (localProps.containsKey("KEYSTORE_PATH")) {
                storeFile = file(localProps["KEYSTORE_PATH"] as String)
                storePassword = localProps["KEYSTORE_PASSWORD"] as String
                keyAlias = localProps["KEY_ALIAS"] as String
                keyPassword = localProps["KEY_PASSWORD"] as String
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            if (localProps.containsKey("KEYSTORE_PATH")) {
                signingConfig = signingConfigs.getByName("release")
            }
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            ndk {
                debugSymbolLevel = "FULL"
            }
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }

    lint {
        abortOnError = false
        checkReleaseBuilds = false
    }
}

tasks.register<Copy>("syncDictionaryWord") {
    from("../app/src/main/java/com/philornot/slownikjezykatrudnego/data/model/DictionaryWord.kt")
    into("src/main/java/com/philornot/slownikjezykatrudnego/data/model")
}

tasks.register<Copy>("syncDictionaryWordsData") {
    from("../app/src/main/java/com/philornot/slownikjezykatrudnego/data/datasource/DictionaryWordsData.kt")
    into("src/main/java/com/philornot/slownikjezykatrudnego/data/datasource")
}

tasks.named("preBuild") {
    dependsOn("syncDictionaryWord", "syncDictionaryWordsData")
}

kotlin {
    jvmToolchain(21)
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)

    // Jetpack Compose
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)

    // Wear OS & Wear Compose
    implementation(libs.androidx.wear)
    implementation(libs.androidx.wear.compose.foundation)
    implementation(libs.androidx.wear.compose.material)
    implementation(libs.androidx.wear.compose.navigation)

    // Serialization & Coroutines
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.kotlinx.coroutines.android)

    debugImplementation(libs.androidx.ui.tooling)
}
