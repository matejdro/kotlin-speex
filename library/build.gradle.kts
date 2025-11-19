import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.vanniktech.mavenPublish)
}
//val headSha by lazy {
//    project.providers.exec {
//        commandLine("git", "describe", "--always", "--dirty")
//    }.standardOutput.asText.get().trim()
//}
group = "coredevices.speex"
//version = headSha

publishing {
    repositories {
        maven {
            name = "GitHubPackages"
            url = uri("https://maven.pkg.github.com/coredevices/kotlin-speex")
            credentials {
                username = System.getenv("GITHUB_ACTOR")
                password = System.getenv("GITHUB_TOKEN")
            }
        }
    }
}

android {
    externalNativeBuild {
        cmake {
            path("${rootDir}/speex/CMakeLists.txt")
            version = "3.22.1"
        }
    }
}

kotlin {
    androidTarget {
        publishLibraryVariants("release", "debug")
        @OptIn(ExperimentalKotlinGradlePluginApi::class)
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_11)
        }
    }
    macosArm64 {
        val root = project.rootDir
        compilations.getByName("main") {
            cinterops {
                val speex by creating {
                    extraOpts("-libraryPath", "${root}/speex/lib/macos")
                    compilerOpts.add("-I${root}/speex/include")
                    compilerOpts.add("-I${root}/speex/source/include")
                }
            }
        }
    }
    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64()
    ).forEach {
        val root = project.rootDir
        it.compilations.getByName("main") {
            cinterops {
                val speex by creating {
                    extraOpts("-libraryPath", "${root}/speex/lib/ios")
                    compilerOpts.add("-I${root}/speex/include")
                    compilerOpts.add("-I${root}/speex/source/include")
                }
            }
        }
    }

    sourceSets {
        val commonMain by getting {
            dependencies {
                //put your multiplatform dependencies here
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(libs.kotlin.test)
                implementation(libs.kotlinx.io)
            }
        }
    }
}

android {
    namespace = "coredevices.speex"
    ndkVersion = "28.1.13356709"
    compileSdk = libs.versions.android.compileSdk.get().toInt()
    defaultConfig {
        minSdk = libs.versions.android.minSdk.get().toInt()
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

mavenPublishing {
    coordinates(group.toString(), "speex", version.toString())
}
