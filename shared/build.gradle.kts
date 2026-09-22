plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.multiplatform")
    id("org.jetbrains.kotlin.plugin.serialization")
}

kotlin {
    androidTarget {
        compilerOptions {
            jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
        }
    }

    iosX64 {
        binaries.framework { baseName = "sharedKit" }
    }
    iosArm64 {
        binaries.framework { baseName = "sharedKit" }
    }
    iosSimulatorArm64 {
        binaries.framework { baseName = "sharedKit" }
    }

    sourceSets {
        commonMain.dependencies {
            // Coroutines / serialization — same versions as :app.
            implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.9.0")
            implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.7.3")

            // Ktor — multiplatform core only. Engines are per-platform below.
            implementation("io.ktor:ktor-client-core:3.0.3")
            implementation("io.ktor:ktor-client-content-negotiation:3.0.3")
            implementation("io.ktor:ktor-serialization-kotlinx-json:3.0.3")
            implementation("io.ktor:ktor-client-websockets:3.0.3")

            // ViewModel — multiplatform base artifact (not -ktx).
            // See app/build.gradle.kts: lifecycle-viewmodel-compose is Android-only.
            implementation("androidx.lifecycle:lifecycle-viewmodel:2.8.7")
        }
        androidMain.dependencies {
            implementation("io.ktor:ktor-client-okhttp:3.0.3")
        }
        iosMain.dependencies {
            implementation("io.ktor:ktor-client-darwin:3.0.3")
        }
        commonTest.dependencies {
            implementation("org.jetbrains.kotlin:kotlin-test:2.3.20")
            implementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.10.2")
            implementation("io.ktor:ktor-client-mock:3.0.3")
        }
    }
}

android {
    namespace = "com.music.bitchord.shared"
    compileSdk = 36

    defaultConfig {
        minSdk = 26
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

// Framework name consumed by iosApp (ContentView.swift imports sharedKit).
// Gradle task :shared:embedAndSignAppleFrameworkForXcode is wired into the
// Xcode Run Script phase so Xcode builds never go stale.
