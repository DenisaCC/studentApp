plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id ("com.google.gms.google-services")
}

android {
    namespace = "com.example.licentaapp"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.licentaapp"
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

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
        kotlinCompilerExtensionVersion = "1.5.1"
    }
    packaging {
        resources {
            exclude("META-INF/DEPENDENCIES")
            exclude("META-INF/LICENSE")
            exclude("META-INF/LICENSE.md")
            exclude("META-INF/license.txt")
            exclude("META-INF/NOTICE")
            exclude("META-INF/NOTICE.md")
            exclude("META-INF/notice.txt")
            exclude("META-INF/ASL2.0")
            exclude("META-INF/*.kotlin_module")
        }
    }
}

dependencies {

    implementation (platform("com.google.firebase:firebase-bom:33.0.0"))
    implementation ("com.sun.mail:android-mail:1.6.7")
    implementation ("com.sun.mail:android-activation:1.6.7")
    implementation ("com.squareup.retrofit2:retrofit:2.9.0")
    implementation ("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation ("org.apache.poi:poi:5.2.3")
    implementation ("org.apache.poi:poi-ooxml:5.2.3")
    implementation("androidx.core:core-ktx:1.10.1")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.6.1")
    implementation("androidx.activity:activity-compose:1.7.0")
    implementation(platform("androidx.compose:compose-bom:2023.08.00"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation ("com.github.bumptech.glide:glide:4.12.0")
    implementation("com.google.android.material:material:1.9.0")
    implementation(files("C:\\Users\\Denisa\\AndroidStudioProjects\\licentaApp\\app\\libs\\jtds-1.3.1.jar"))
    implementation("androidx.activity:activity:1.8.0")
    implementation("com.google.firebase:firebase-common:21.0.0")
    implementation("com.google.firebase:firebase-auth:23.0.0")
    implementation("com.google.firebase:firebase-database:21.0.0")
    implementation("com.google.firebase:firebase-crashlytics-buildtools:3.0.0")
    implementation("com.android.volley:volley:1.2.1")
    implementation("com.google.firebase:firebase-messaging:24.0.0")
    releaseImplementation("com.android.support:appcompat-v7:28.0.0")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    androidTestImplementation(platform("androidx.compose:compose-bom:2023.08.00"))
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")

}