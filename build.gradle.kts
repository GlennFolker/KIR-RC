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
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("androidx.fragment:fragment-ktx:1.7.0")
}

android {
    namespace = "com.github.glennfolker.kirrc"
    compileSdk = 34
    buildToolsVersion = "34.0.0"

    defaultConfig {
        applicationId = "com.github.glennfolker.kirrc"
        minSdk = 21
        targetSdk = 34
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

            kotlin.srcDir(layout.projectDirectory.dir("src"))
            res.srcDir(layout.projectDirectory.dir("res"))
            jniLibs.srcDir(layout.projectDirectory.dir("libs"))
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
    mustRunAfter(tasks.getByName("installDebug"), tasks.getByName("installRelease"))
    commandLine("adb", "shell", "am", "start", "-n", "com.github.glennfolker.kirrc/com.github.glennfolker.kirrc.KIRActivity")
}
