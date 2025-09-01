import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.targets.js.webpack.KotlinWebpackConfig
import org.gradle.internal.os.OperatingSystem

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.composeHotReload)
}

kotlin {
    androidTarget {
        @OptIn(ExperimentalKotlinGradlePluginApi::class)
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_11)
        }
    }

    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "ComposeApp"
            isStatic = true
        }
    }

    jvm()

    // ✅ Only add wasmJs target when not on Windows
    if (!OperatingSystem.current().isWindows) {
        @OptIn(ExperimentalWasmDsl::class)
        wasmJs {
            outputModuleName.set("composeApp")
            browser {
                val rootDirPath = project.rootDir.path
                val projectDirPath = project.projectDir.path
                commonWebpackConfig {
                    outputFileName = "composeApp.js"
                    devServer = (devServer ?: KotlinWebpackConfig.DevServer()).apply {
                        static = (static ?: mutableListOf()).apply {
                            add(rootDirPath)
                            add(projectDirPath)
                        }
                    }
                }
            }
            binaries.executable()
        }
    }

    sourceSets {
        commonMain.dependencies {
            // Compose
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.ui)
            implementation(compose.components.resources)
            implementation(compose.components.uiToolingPreview)
            implementation(compose.materialIconsExtended)
            implementation("com.russhwolf:multiplatform-settings:1.1.1")

            // Voyager Navigation
            implementation("cafe.adriel.voyager:voyager-navigator:1.0.0-rc05")
            implementation("org.jetbrains.androidx.navigation:navigation-compose:2.9.0-beta05")

            // Koin (shared DI)
            implementation(libs.koin.core)
            implementation("io.insert-koin:koin-compose:4.1.0") // ✅ multiplatform compose support

            implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.4.0")

            // Ktor (common)
            implementation(libs.ktor.client.core)
            implementation(libs.ktor.client.logging)
            implementation(libs.ktor.client.contentNegotiation)
            implementation(libs.ktor.serialization.kotlinx.json)

            // Image loading
            implementation("media.kamel:kamel-image:0.9.5")
            implementation("io.coil-kt.coil3:coil-compose:3.3.0")
            implementation("io.coil-kt.coil3:coil-network-ktor3:3.3.0")

            // Coroutines
            implementation(libs.kotlinx.coroutines.core)

            // Shared module
            implementation(project(":shared"))
        }

        androidMain.dependencies {
            implementation(compose.preview)
            implementation(libs.androidx.activity.compose)
            implementation(libs.androidx.navigation.compose)
            implementation(libs.androidx.material3)

            // Koin (Android-specific)
            implementation(libs.koin.android)
            implementation(libs.koin.core.viewmodel.android)

            // Ktor (Android engine)
            implementation(libs.ktor.client.okhttp)
            implementation(libs.ktor.client.cio)

            implementation(libs.sqldelight.driver.android)

            implementation("io.insert-koin:koin-androidx-compose:3.4.3")
            // Security
            implementation("androidx.security:security-crypto:1.1.0")
            implementation("com.russhwolf:multiplatform-settings:1.1.1")

            // Coil (with OkHttp)
            implementation("io.coil-kt.coil3:coil-network-okhttp:3.3.0")
        }

        iosMain.dependencies {
            // Ktor (iOS engine)
            implementation(libs.ktor.client.darwin)
//            implementation("com.russhwolf:multiplatform-settings-ios:1.1.1")


        }

        jvmMain.dependencies {
            implementation(compose.desktop.currentOs)
            implementation(libs.kotlinx.coroutinesSwing)

            implementation("com.russhwolf:multiplatform-settings:1.1.1")


            // Ktor (JVM engine)
            implementation(libs.ktor.client.cio)
        }

        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
    }
}

android {
    namespace = "org.robiul.kmprecipeapp"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    defaultConfig {
        applicationId = "org.robiul.kmprecipeapp"
        minSdk = libs.versions.android.minSdk.get().toInt()
        targetSdk = libs.versions.android.targetSdk.get().toInt()
        versionCode = 1
        versionName = "1.0"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    debugImplementation(compose.uiTooling)
}

compose.desktop {
    application {
        mainClass = "org.robiul.kmprecipeapp.MainKt"
        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "org.robiul.kmprecipeapp"
            packageVersion = "1.0.0"
        }
    }
}
