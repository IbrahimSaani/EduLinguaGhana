import com.google.firebase.appdistribution.gradle.firebaseAppDistribution
import java.util.Properties
import java.io.FileInputStream

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.google.services)
    alias(libs.plugins.firebase.crashlytics)
    alias(libs.plugins.firebase.appdistribution)
}

android {
    namespace = "com.edulinguaghana"
    compileSdk = 35

    val secretsPropertiesFile = rootProject.file("secrets.properties")
    val secretsProperties = Properties()
    if (secretsPropertiesFile.exists()) {
        secretsProperties.load(FileInputStream(secretsPropertiesFile))
    }

    defaultConfig {
        applicationId = "com.edulinguaghana"
        minSdk = 23
        targetSdk = 35
        versionCode = 11
        versionName = "1.1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        ndk {
            abiFilters.addAll(setOf("armeabi-v7a", "arm64-v8a", "x86", "x86_64"))
        }

        // Inject secrets from secrets.properties
        resValue("string", "facebook_app_id", secretsProperties.getProperty("FACEBOOK_APP_ID", ""))
        resValue("string", "facebook_client_token", secretsProperties.getProperty("FACEBOOK_CLIENT_TOKEN", ""))
        resValue("string", "fb_login_protocol_scheme", secretsProperties.getProperty("FB_LOGIN_PROTOCOL_SCHEME", ""))

        buildConfigField("String", "GHANA_LP_TTS_API_KEY", "\"${secretsProperties.getProperty("GHANA_LP_TTS_API_KEY", "")}\"")
    }

    buildFeatures {
        buildConfig = true
        resValues = true
    }

    signingConfigs {
        create("release") {
            val keystorePropertiesFile = rootProject.file("keystore.properties")
            val keystoreProperties = Properties()
            if (keystorePropertiesFile.exists()) {
                keystoreProperties.load(FileInputStream(keystorePropertiesFile))
            }

            keystoreProperties.getProperty("RELEASE_STORE_FILE")?.let {
                storeFile = file(it)
            }
            storePassword = keystoreProperties.getProperty("RELEASE_STORE_PASSWORD")
            keyAlias = keystoreProperties.getProperty("RELEASE_KEY_ALIAS")
            keyPassword = keystoreProperties.getProperty("RELEASE_KEY_PASSWORD")
        }
    }

    buildTypes {
        release {
            signingConfig = signingConfigs.getByName("release")
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            firebaseAppDistribution {
                appId = "1:340016497126:android:36b6e04f90691927389627"
                testers = "ibrahimsaani41@gmail.com, quistkelvin32@gmail.com, 41jamesanderson@gmail.com, lovejoycelyn32@gmail.com, rhozaselorm@gmail.com, selormeyphinegad@gmail.com, ucheemmauel539@gmail.com, jnrhoshea@gmail.com, manuelowusu47@gmail.com"
                releaseNotes = "ui improvements for final testing to be deployed."
            }
        }
        create("beta") {
            initWith(getByName("release"))
            matchingFallbacks += listOf("release")
            firebaseAppDistribution {
                appId = "1:340016497126:android:36b6e04f90691927389627"
                testers = "ibrahimsaani41@gmail.com, quistkelvin32@gmail.com, 41jamesanderson@gmail.com, lovejoycelyn32@gmail.com, rhozaselorm@gmail.com, selormeyphinegad@gmail.com, ucheemmauel539@gmail.com, jnrhoshea@gmail.com, manuelowusu47@gmail.com"
                releaseNotes = "ui improvements for final testing to be deployed."
            }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    lint {
        abortOnError = false
        disable += setOf("LongLogTag", "NewApi")
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
        jniLibs {
            useLegacyPackaging = true
        }
    }
}

dependencies {
    implementation(libs.core.splashscreen)
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation(libs.lottie)
    implementation(libs.glide)
    implementation("androidx.viewpager2:viewpager2:1.0.0")
    implementation("androidx.cardview:cardview:1.0.0")
    implementation("androidx.annotation:annotation:1.9.1")
    implementation("com.google.code.gson:gson:2.11.0")

    // HTTP client for GhanaLP TTS API
    implementation("com.squareup.okhttp3:okhttp:4.12.0")

    // QR Code generation and scanning
    implementation("com.google.zxing:core:3.5.2")
    implementation("com.journeyapps:zxing-android-embedded:4.3.0")

    // Firebase Authentication
    implementation(platform("com.google.firebase:firebase-bom:34.14.1"))
    implementation("com.google.firebase:firebase-auth")
    implementation("com.google.firebase:firebase-database")
    implementation("com.google.firebase:firebase-crashlytics")
    implementation("com.google.firebase:firebase-messaging")
    implementation("com.google.firebase:firebase-installations")
    implementation("com.google.android.gms:play-services-auth:21.3.0")

    // Facebook Login
    implementation("com.facebook.android:facebook-login:18.0.0")

    // Celebratory effects
    implementation("nl.dionsegijn:konfetti-xml:2.0.4")

    // WorkManager for background tasks and notifications
    implementation("androidx.work:work-runtime:2.11.2")

    // Firebase App Distribution Feedback
    implementation(libs.firebase.appdistribution.api)
    add("betaImplementation", libs.firebase.appdistribution.sdk)
    debugImplementation(libs.firebase.appdistribution.sdk)

    testImplementation(libs.junit)

    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}