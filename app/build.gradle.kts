import java.util.Properties
import java.io.FileInputStream

plugins {
    id("com.android.application")
    id("com.chaquo.python")
}

val keystorePropsFile = rootProject.file("release.properties")
val keystoreProps = Properties()

if (keystorePropsFile.exists()) {
    keystoreProps.load(FileInputStream(keystorePropsFile))
}

val hasValidSigningProps = keystorePropsFile.exists().also { exists ->
    if (exists) {
        FileInputStream(keystorePropsFile).use { keystoreProps.load(it) }
    }
}.let {
    listOf(
        "storeFile",
        "storePassword",
        "keyAlias",
        "keyPassword"
    ).all { key ->
        keystoreProps[key] != null
    }
}

android {
    namespace = "com.python.ide"
    compileSdk = 36

    lint {
        checkReleaseBuilds = false
    }

    signingConfigs {
        if (hasValidSigningProps) {
            create("release") {
                storeFile = rootProject.file(keystoreProps["storeFile"] as String)
                storePassword = keystoreProps["storePassword"] as String
                keyAlias = keystoreProps["keyAlias"] as String
                keyPassword = keystoreProps["keyPassword"] as String
            }
        }
    }

    defaultConfig {
        applicationId = "com.python.ide"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.2"

        vectorDrawables {
            useSupportLibrary = true
        }

        ndk {
            abiFilters += listOf(
                "arm64-v8a",
                "armeabi-v7a"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildTypes {
        release {
            if (hasValidSigningProps) {
                signingConfig = signingConfigs.getByName("release")
            }
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    buildFeatures {
        viewBinding = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.10"
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
            excludes += "META-INF/kotlinx_coroutines_core.version"
        }
    }

    configurations.all {
        resolutionStrategy {
            force("org.jetbrains.kotlin:kotlin-stdlib:1.9.22")
            force("org.jetbrains.kotlin:kotlin-stdlib-jdk7:1.9.22")
            force("org.jetbrains.kotlin:kotlin-stdlib-jdk8:1.9.22")

            force("androidx.collection:collection:1.4.2")
            force("androidx.annotation:annotation:1.8.1")
            force("androidx.core:core-ktx:1.8.0")
            force("androidx.lifecycle:lifecycle-runtime-ktx:2.3.1")
            force("androidx.collection:collection-ktx:1.4.2")
        }
    }
}

chaquopy {
    defaultConfig {
        version = "3.11"
    }
}

tasks.withType<JavaCompile> {
    options.compilerArgs.add("-Xlint:deprecation")
}

dependencies {
    implementation("androidx.startup:startup-runtime:1.1.1")
    implementation("androidx.interpolator:interpolator:1.0.0")
    implementation("com.google.android.material:material:1.9.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("io.github.Rosemoe.sora-editor:editor:0.23.5")
}