plugins {
    alias(libs.plugins.android.application)

}

android {
    namespace = "com.example.phmeter"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.phmeter"
        minSdk = 24
        targetSdk = 35
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
    // Your existing version-catalog imports
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)

    // Networking: OkHttp
    implementation("com.squareup.okhttp3:okhttp:4.11.0")

    // Room (Persistence)
    implementation("androidx.room:room-runtime:2.5.1")
    annotationProcessor("androidx.room:room-compiler:2.5.1")
    // (Optional but handy extensions)
    implementation("androidx.room:room-ktx:2.5.1")

    // Navigation (if you plan to use AndroidX navigation components; optional for manual FragmentTransaction)
    implementation("androidx.navigation:navigation-fragment:2.5.3")
    implementation("androidx.navigation:navigation-ui:2.5.3")

    // Testing
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}