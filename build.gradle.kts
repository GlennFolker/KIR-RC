import java.io.*
import java.util.*

plugins {
    id("com.android.application") version "8.2.0"
    kotlin("android") version "1.9.23"
}

repositories {
    google()
    mavenCentral()
}

dependencies {
    implementation("androidx.activity:activity-ktx:1.9.0")
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("androidx.fragment:fragment-ktx:1.8.0")
    implementation("androidx.recyclerview:recyclerview:1.3.2")
}

android {
    namespace = "com.github.glennfolker.kirrc"
    compileSdk = 35
    buildToolsVersion = "35.0.0"

    defaultConfig {
        applicationId = "com.github.glennfolker.kirrc"
        minSdk = 21
        targetSdk = 35
        versionCode = 1
        versionName = "0.1.0"
    }

    signingConfigs {
        register("release") {
            val sign = File("signing.properties")
            if(sign.exists()) {
                val props = Properties()
                BufferedReader(sign.reader(Charsets.UTF_8)).use { props.load(it) }

                storeFile = File(props.getProperty("store.file"))
                storePassword = props.getProperty("store.password")
                keyAlias = props.getProperty("key.alias")
                keyPassword = props.getProperty("key.password")
            }
        }
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = true
            signingConfig = signingConfigs.getByName("release")
        }
    }

    sourceSets {
        getByName("main").apply {
            manifest.srcFile(layout.projectDirectory.file("AndroidManifest.xml"))

            kotlin.setSrcDirs(listOf(layout.projectDirectory.dir("src")))
            res.setSrcDirs(listOf(layout.projectDirectory.dir("res")))
            jniLibs.setSrcDirs(listOf(layout.projectDirectory.dir("libs")))
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    kotlinOptions {
        jvmTarget = "11"
    }
}

tasks.register<Exec>("run") {
    dependsOn(tasks.getByName("installDebug"))
    commandLine("adb", "shell", "am", "start", "-n", "com.github.glennfolker.kirrc/com.github.glennfolker.kirrc.KIRActivity")
}
