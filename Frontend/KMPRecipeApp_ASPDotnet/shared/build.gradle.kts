import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.targets.js.webpack.KotlinWebpackConfig
import org.gradle.internal.os.OperatingSystem

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.kotlinSerialization)
    id("app.cash.sqldelight") version "2.1.0"
}

kotlin {
    androidTarget {
        @OptIn(ExperimentalKotlinGradlePluginApi::class)
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_11)
        }
    }

    iosX64()
    iosArm64()
    iosSimulatorArm64()

    jvm()

    if (!org.gradle.internal.os.OperatingSystem.current().isWindows) {
        @OptIn(ExperimentalWasmDsl::class)
        wasmJs {
            browser {
                val rootDirPath = project.rootDir.path
                val projectDirPath = project.projectDir.path
                commonWebpackConfig {
                    devServer = (devServer ?: KotlinWebpackConfig.DevServer()).apply {
                        static = (static ?: mutableListOf()).apply {
                            add(rootDirPath)
                            add(projectDirPath)
                        }
                    }
                }
            }
        }
    }

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(libs.koin.core)

                // Ktor (MockEngine only in Phase 6)
                implementation(libs.ktor.client.core)
                implementation(libs.ktor.client.logging)
                implementation(libs.ktor.client.contentNegotiation)
                implementation(libs.ktor.serialization.kotlinx.json)
                implementation(libs.ktor.client.mock)

                // SQLDelight (runtime + coroutines)
                implementation(libs.sqldelight.runtime)
                implementation(libs.sqldelight.coroutines)

                // Kotlinx
                implementation(libs.kotlinx.serialization.json)
                implementation(libs.kotlinx.datetime)

                implementation("io.ktor:ktor-client-core:2.3.4") // core Ktor client

            }
        }
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test"))
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.10.2")
                implementation(libs.ktor.client.mock)
            }
        }
        val androidMain by getting {
            dependencies {
                implementation(libs.sqldelight.driver.android)
                implementation(libs.ktor.client.okhttp)

            }
        }
        val jvmMain by getting {
            dependencies {
                implementation("app.cash.sqldelight:sqlite-driver:2.1.0")
                implementation(libs.ktor.client.okhttp)
                implementation("io.ktor:ktor-client-core:2.3.4") // core Ktor
                implementation("io.ktor:ktor-client-content-negotiation:2.3.4")
                implementation("io.ktor:ktor-serialization-kotlinx-json:2.3.4")
                implementation("io.ktor:ktor-client-logging:2.3.4")
                implementation("io.ktor:ktor-client-auth:2.3.4")

            }
        }

        val iosX64Main by getting
        val iosArm64Main by getting
        val iosSimulatorArm64Main by getting
        val iosMain by creating {
            dependsOn(commonMain)
            iosX64Main.dependsOn(this)
            iosArm64Main.dependsOn(this)
            iosSimulatorArm64Main.dependsOn(this)
            dependencies {
                implementation(libs.sqldelight.driver.native)
                implementation(libs.ktor.client.darwin)

            }
        }

        // wasmJsMain uses only stubs; no engine deps in Phase 6
        if (!OperatingSystem.current().isWindows) {
            val wasmJsMain by getting {
                dependencies {
                    implementation(libs.ktor.client.js) // will be guarded in code; no auth endpoints on wasm for now
                }
            }
        }

    }

}

android {
    namespace = "org.robiul.kmprecipeapp.shared"
    compileSdk = libs.versions.android.compileSdk.get().toInt()
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    defaultConfig {
        minSdk = libs.versions.android.minSdk.get().toInt()
    }
}

sqldelight {
    databases {
        create("AppDatabase") {
            packageName.set("org.robiul.kmprecipeapp.db")
            verifyMigrations.set(!org.gradle.internal.os.OperatingSystem.current().isWindows)
            // important on Windows

        }
    }
}

// Workaround: disable VerifyMigration tasks on Windows
tasks.withType<app.cash.sqldelight.gradle.VerifyMigrationTask>().configureEach {
    onlyIf { !org.gradle.internal.os.OperatingSystem.current().isWindows }
}