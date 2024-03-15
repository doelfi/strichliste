plugins {
    id("com.android.application")
}

android {
    namespace = "com.example.strichliste"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.strichliste"
        minSdk = 30
        targetSdk = 34
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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
}

dependencies {

    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("androidx.activity:activity:1.8.2")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.7.0")
    implementation("com.google.android.material:material:1.11.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("androidx.room:room-common:2.6.1")
    implementation(files("libs/poi-bin-5.2.3/poi-5.2.3.jar"))
    implementation(files("libs/poi-bin-5.2.3/lib/commons-io-2.11.0.jar"))
    implementation(files("libs/poi-bin-5.2.3/lib/log4j-api-2.18.0.jar"))
    implementation(files("libs/poi-bin-5.2.3/poi-ooxml-5.2.3.jar"))
    implementation(files("libs/poi-bin-5.2.3/lib/SparseBitSet-1.2.jar"))
    implementation(files("libs/poi-bin-5.2.3/ooxml-lib/xmlbeans-5.1.1.jar"))
    implementation(files("libs/poi-bin-5.2.3/lib/commons-collections4-4.4.jar"))
    implementation(files("libs/poi-bin-5.2.3/ooxml-lib/commons-compress-1.21.jar"))
    implementation(files("libs/poi-bin-5.2.3/poi-ooxml-full-5.2.3.jar"))
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")

    // for database
    val roomVersion = "2.6.1"

    implementation("androidx.room:room-runtime:$roomVersion")
    annotationProcessor("androidx.room:room-compiler:$roomVersion")
}