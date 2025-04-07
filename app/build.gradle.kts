plugins {
    alias(libs.plugins.android.application)
    id("com.google.gms.google-services")
}

android {
    namespace = "com.example.chatapp"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.chatapp"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0.0"

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
    buildFeatures {
        viewBinding = true
    }

    packaging {
        resources {
            resources.excludes.add("META-INF/NOTICE.md")
            resources.excludes.add("META-INF/LICENSE.md")
            resources.excludes.add("META-INF/INDEX.LIST")
            resources.excludes.add("META-INF/DEPENDENCIES")
            resources.excludes.add("META-INF/io.netty.versions.properties")

            /*pickFirsts += ["META-INF/LICENSE.txt"]
            excludes += ["META-INF/NOTICE.md", "META-INF/LICENSE.md", "META-INF/INDEX.LIST", "META-INF/DEPENDENCIES", "META-INF/io.netty.versions.properties"]*/
        }
        //resources.excludes.add("META-INF/*")
    }


}

dependencies {

    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.constraintlayout)
    implementation(libs.lifecycle.livedata.ktx)
    implementation(libs.lifecycle.viewmodel.ktx)
    implementation(libs.navigation.fragment)
    implementation(libs.navigation.ui)
    implementation(libs.activity)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
    implementation(libs.camera.core)
    implementation(libs.camera.lifecycle)
    implementation(libs.camera.view)
    implementation(platform("com.google.firebase:firebase-bom:32.7.0"))
    implementation("com.google.firebase:firebase-messaging")
    implementation("com.google.firebase:firebase-firestore")
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-scalars:2.9.0")
    implementation(libs.google.auth.library.oauth2.http)
    implementation("com.google.firebase:firebase-analytics:21.3.0")


    // Customs
    implementation("com.makeramen:roundedimageview:2.3.0")
    implementation("com.github.NaikSoftware:StompProtocolAndroid:1.6.6")
    implementation("com.squareup.okhttp3:okhttp:3.12.1")
    implementation("io.github.webrtc-sdk:android:125.6422.07")
    // RxJava
    implementation("io.reactivex.rxjava2:rxjava:2.2.5")
    implementation("io.reactivex.rxjava2:rxandroid:2.1.0")
    implementation("com.squareup.retrofit2:converter-gson:2.5.0")
    implementation("com.squareup.retrofit2:adapter-rxjava2:2.5.0")
    implementation("com.squareup.retrofit2:retrofit:2.5.0")
    // video
    implementation("com.google.android.exoplayer:exoplayer:2.18.2")

}

dependencies {
    val room_version = "2.6.1"

    implementation("androidx.room:room-runtime:$room_version")

    // If this project only uses Java source, use the Java annotationProcessor
    // No additional plugins are necessary
    annotationProcessor("androidx.room:room-compiler:$room_version")

    // optional - RxJava2 support for Room
    implementation("androidx.room:room-rxjava2:$room_version")

    // optional - RxJava3 support for Room
    implementation("androidx.room:room-rxjava3:$room_version")

    // optional - Guava support for Room, including Optional and ListenableFuture
    implementation("androidx.room:room-guava:$room_version")

    // optional - Test helpers
    testImplementation("androidx.room:room-testing:$room_version")

    // optional - Paging 3 Integration
    implementation("androidx.room:room-paging:$room_version")
}

// OkHttp request
dependencies {
    val okhttp_version = "4.12.0"
    implementation("com.squareup.okhttp3:okhttp:$okhttp_version")
}

dependencies {  
    implementation("androidx.security:security-crypto:1.1.0-alpha03")
}

dependencies {
    implementation("commons-codec:commons-codec:1.15")
    implementation("io.jsonwebtoken:jjwt-api:0.11.2")
}

dependencies {
    // Retrofit
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("com.squareup.okhttp3:okhttp:4.9.3")
    implementation("com.squareup.okhttp3:logging-interceptor:4.9.3")

    // For handling Reactive streams (optional but recommended)
    implementation("io.reactivex.rxjava3:rxjava:3.1.5")
    implementation("io.reactivex.rxjava3:rxandroid:3.0.0")
    implementation("com.squareup.retrofit2:adapter-rxjava3:2.9.0")
}

dependencies {
    val work_version = "2.8.1"
    implementation("androidx.work:work-runtime:$work_version")
    implementation("androidx.startup:startup-runtime:1.1.1")
    implementation(libs.work.runtime)
}

dependencies {
    implementation("com.cloudinary:cloudinary-android:3.0.2")
    implementation("com.github.bumptech.glide:glide:4.16.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("androidx.navigation:navigation-fragment:2.7.7")
    implementation("androidx.navigation:navigation-ui:2.7.7")
}

dependencies {

    implementation("com.github.bumptech.glide:glide:4.16.0")
    annotationProcessor("com.github.bumptech.glide:compiler:4.16.0")
}

dependencies {
    implementation(libs.swiperefreshlayout)
    implementation(libs.play.services.vision)
    implementation(libs.play.services.vision.common)
    implementation("androidx.camera:camera-core:1.3.0")
    implementation ("androidx.camera:camera-camera2:1.3.0")
    implementation ("androidx.camera:camera-lifecycle:1.3.0")
    implementation ("androidx.camera:camera-view:1.3.0")

    implementation ("de.hdodenhof:circleimageview:3.1.0")

}
