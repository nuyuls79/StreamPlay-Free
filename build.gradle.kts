import com.android.build.gradle.BaseExtension
import com.lagradost.cloudstream3.gradle.CloudstreamExtension
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinJvmCompile

buildscript {

    // ========== PERBAIKAN 1 ==========
    // Ganti master-SNAPSHOT dengan commit hash yang valid
    val cloudstreamGradlePluginVersion = project
        .findProperty("cloudstream.gradle.plugin.version")
        ?.toString()
        ?: "81b1d424d2"   // commit hash terbaru yang tersedia
    // =================================

    val kotlinVersion = project
        .findProperty("kotlin.version")
        ?.toString()
        ?: "2.3.0"

    val androidGradlePluginVersion = project
        .findProperty("android.gradle.plugin.version")
        ?.toString()
        ?: "8.7.3"

    repositories {
        google()
        mavenCentral()
        maven("https://jitpack.io")
    }

    dependencies {
        classpath(
            "com.android.tools.build:gradle:$androidGradlePluginVersion"
        )
        classpath(
            "com.github.recloudstream:gradle:$cloudstreamGradlePluginVersion"
        )
        classpath(
            "org.jetbrains.kotlin:kotlin-gradle-plugin:$kotlinVersion"
        )
    }
}

val cloudstreamApiVersion = providers
    .gradleProperty("cloudstream.api.version")
    .orElse("pre-release")
    .get()

val kotlinxCoroutinesVersion = providers
    .gradleProperty("kotlinx.coroutines.version")
    .orElse("1.10.1")
    .get()

val kotlinxSerializationVersion = providers
    .gradleProperty("kotlinx.serialization.version")
    .orElse("1.7.3")
    .get()

val androidCompileSdkVersion = providers
    .gradleProperty("android.compileSdk.version")
    .orElse("35")
    .get()
    .toInt()

val androidTargetSdkVersion = providers
    .gradleProperty("android.targetSdk.version")
    .orElse(androidCompileSdkVersion.toString())
    .get()
    .toInt()

allprojects {
    repositories {
        google()
        mavenCentral()
        maven("https://jitpack.io")
    }
}

fun Project.cloudstream(
    configuration: CloudstreamExtension.() -> Unit
) = extensions
    .getByName<CloudstreamExtension>("cloudstream")
    .configuration()

fun Project.android(
    configuration: BaseExtension.() -> Unit
) = extensions
    .getByName<BaseExtension>("android")
    .configuration()

subprojects {

    apply(plugin = "com.android.library")
    apply(plugin = "kotlin-android")
    apply(plugin = "com.lagradost.cloudstream3.gradle")

    cloudstream {
        setRepo(
            System.getenv("GITHUB_REPOSITORY")
                ?: "https://github.com/duro92/ExtCloud"
        )
        authors = listOf("sad25kag")
    }

    android {

        namespace = "com.excloud"

        defaultConfig {

            minSdk = 21
            compileSdkVersion(androidCompileSdkVersion)
            targetSdk = androidTargetSdkVersion

            // ========== PERBAIKAN 2 ==========
            // Aktifkan BuildConfig untuk library module
            buildConfig = true
            // =================================
        }

        compileOptions {
            sourceCompatibility = JavaVersion.VERSION_17
            targetCompatibility = JavaVersion.VERSION_17
        }

        tasks.withType<KotlinJvmCompile>() {
            compilerOptions {
                jvmTarget.set(JvmTarget.JVM_17)
                freeCompilerArgs.addAll(
                    "-Xno-call-assertions",
                    "-Xno-param-assertions",
                    "-Xno-receiver-assertions"
                )
            }
        }
    }

    dependencies {
        val cloudstream by configurations
        val implementation by configurations

        cloudstream("com.lagradost:cloudstream3:$cloudstreamApiVersion")

        implementation(kotlin("stdlib"))
        implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:$kotlinxCoroutinesVersion")
        implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$kotlinxCoroutinesVersion")
        implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:$kotlinxSerializationVersion")

        implementation("com.github.Blatzar:NiceHttp:0.4.13")
        implementation("com.squareup.okhttp3:okhttp:4.12.0")
        implementation("org.jsoup:jsoup:1.18.3")

        implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.16.0")
        implementation("com.fasterxml.jackson.core:jackson-databind:2.16.0")
        implementation("com.google.code.gson:gson:2.11.0")

        implementation("com.faendir.rhino:rhino-android:1.6.0")
        implementation("app.cash.quickjs:quickjs-android:0.9.2")
        implementation("me.xdrop:fuzzywuzzy:1.4.0")
        implementation("androidx.core:core-ktx:1.16.0")
    }
}

task<Delete>("clean") {
    delete(rootProject.layout.buildDirectory)
}