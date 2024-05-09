plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.jetbrainsKotlinAndroid)
}

android {

    signingConfigs {
        create("release") {
            storeFile = file("/Users/gim-yeonghyeon/Documents/GitHub/super-kitchen/skapp.jks")
            storePassword = "1q2w3e4r5t!"
            keyPassword = "5t4r3e2w1q!"
            keyAlias = "smcs"
        }
    }

    namespace = "com.focusone.skscms"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.focusone.skscms"
        minSdk = 23
        targetSdk = 33
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
    buildTypes {
        release {
            isMinifyEnabled = true
            applicationIdSuffix = ".prod"
            versionNameSuffix = "-prod"
            buildConfigField("String", "MAIN_URL", "\"https://scms.superkitchen.co.kr\"")
            signingConfig = signingConfigs.getByName("release")
                    proguardFiles(
                        getDefaultProguardFile("proguard-android-optimize.txt"),
                        "proguard-rules.pro"
                    )
        }
        debug {
            isMinifyEnabled = false
            applicationIdSuffix = ".dev"
            versionNameSuffix = "-demo"
            buildConfigField("String", "MAIN_URL", "\"https://dev-scms.superkitchen.kr\"")
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
        viewBinding = true
        buildConfig = true
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    //WebView
    implementation("androidx.webkit:webkit:1.11.0")

    //Barcode scanner
    implementation("com.journeyapps:zxing-android-embedded:4.3.0")

    //Permission
    implementation("io.github.ParkSangGwon:tedpermission-normal:3.3.0")
}