import java.util.Properties
import org.gradle.api.tasks.Copy
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

val generatedChangelogAssetsDir =
    layout.buildDirectory.dir(
        "generated/changelogAssets"
    )

val generateChangelogAsset by tasks.registering(
    Copy::class
) {
    from(
        rootProject.file(
            "CHANGELOG.md"
        )
    )
    into(
        generatedChangelogAssetsDir
    )
}

android {
    namespace = "com.saney.ytmimporter"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.saney.ytmimporter"
        minSdk = 26
        targetSdk = 36
        versionCode = 95
        versionName = "1.4.52"
    }

    signingConfigs {
        create("release") {
            val propsFile = rootProject.file("release-signing.properties")
            if (propsFile.exists()) {
                val props = Properties().apply {
                    propsFile.inputStream().use { load(it) }
                }
                storeFile = rootProject.file(props.getProperty("storeFile"))
                storePassword = props.getProperty("storePassword")
                keyAlias = props.getProperty("keyAlias")
                keyPassword = props.getProperty("keyPassword")
            }
        }
    }

    buildTypes {
        debug {
            isMinifyEnabled = false
        }

        release {
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            if (rootProject.file("release-signing.properties").exists()) {
                signingConfig = signingConfigs.getByName("release")
            }
        }
    }

    buildFeatures {
        buildConfig = true
    }

    sourceSets
        .getByName(
            "main"
        )
        .assets
        .srcDir(
            generatedChangelogAssetsDir
        )

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

kotlin {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_17)
    }
}

dependencies {
    implementation("com.google.android.gms:play-services-auth:21.6.0")
    implementation("androidx.core:core:1.15.0")

    testImplementation("junit:junit:4.13.2")
    testImplementation("org.json:json:20240303")
}


tasks
    .named(
        "preBuild"
    )
    .configure {
        dependsOn(
            generateChangelogAsset
        )
    }
