import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.compose.ExperimentalComposeLibrary
import org.jetbrains.kotlin.gradle.targets.js.dsl.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.targets.js.webpack.KotlinWebpackConfig

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.jetbrainsCompose)
    alias(libs.plugins.pluginCompose)
}

val kotlinLanguageVersionOverride = providers.gradleProperty("kotlin_language_version")
    .map(org.jetbrains.kotlin.gradle.dsl.KotlinVersion::fromVersion)
    .orNull
val kotlinApiVersionOverride = providers.gradleProperty("kotlin_api_version")
    .map(org.jetbrains.kotlin.gradle.dsl.KotlinVersion::fromVersion)
    .orNull
val kotlinAdditionalCliOptions = providers.gradleProperty("kotlin_additional_cli_options")
    .map { it.split(" ") }
    .orNull

kotlin {
    androidTarget {
        compilations.all {
            kotlinOptions {
                jvmTarget = "1.8"
            }
        }
    }
    
    jvm("desktop")
    
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

    @OptIn(ExperimentalWasmDsl::class)
    wasmJs {
        outputModuleName.set("composeApp")
        browser {
            commonWebpackConfig {
                outputFileName = "composeApp.js"
                devServer = (devServer ?: KotlinWebpackConfig.DevServer()).apply {
                    static = (static ?: mutableListOf()).apply {
                        // Serve sources to debug inside browser
                        add(project.rootDir.path)
                        add(project.projectDir.path)
                    }
                }
            }
        }
        binaries.executable()
        compilations.configureEach {
            compileTaskProvider.configure {
                if (kotlinLanguageVersionOverride != null) {
                    compilerOptions {
                        languageVersion.set(kotlinLanguageVersionOverride)
                        logger.info("[KUP] ${this@configure.path} : set LV to $kotlinLanguageVersionOverride")
                    }
                }
                if (kotlinApiVersionOverride != null) {
                    compilerOptions {
                        apiVersion.set(kotlinApiVersionOverride)
                        logger.info("[KUP] ${this@configure.path} : set APIV to $kotlinApiVersionOverride")
                    }
                }
                if (kotlinAdditionalCliOptions != null) {
                    compilerOptions {
                        freeCompilerArgs.addAll(kotlinAdditionalCliOptions)
                        logger.info(
                            "[KUP] ${this@configure.path} : added ${
                                kotlinAdditionalCliOptions.joinToString(
                                    " "
                                )
                            }"
                        )
                    }
                }
                compilerOptions {
                    // output reported warnings even in the presence of reported errors
                    freeCompilerArgs.add("-Xreport-all-warnings")
                    logger.info("[KUP] ${this@configure.path} : added -Xreport-all-warnings")
                    // output kotlin.git-searchable names of reported diagnostics
                    freeCompilerArgs.add("-Xrender-internal-diagnostic-names")
                    logger.info("[KUP] ${this@configure.path} : added -Xrender-internal-diagnostic-names")
                    freeCompilerArgs.add("-Wextra")
                    logger.info("[KUP] ${this@configure.path}: added -Wextra")
                    freeCompilerArgs.add("-Xuse-fir-experimental-checkers")
                    logger.info("[KUP] ${this@configure.path}: added -Xuse-fir-experimental-checkers")
                }
            }
        }
    }

    sourceSets {
        val desktopMain by getting
        
        androidMain.dependencies {
            implementation(libs.compose.ui.tooling.preview)
            implementation(libs.androidx.activity.compose)
        }
        desktopMain.dependencies {
            implementation(compose.desktop.currentOs)
        }
        commonMain.dependencies {
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material)
            implementation(compose.ui)
            @OptIn(ExperimentalComposeLibrary::class)
            implementation(compose.components.resources)
        }
    }
}

android {
    namespace = "org.example.project"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    sourceSets["main"].manifest.srcFile("src/androidMain/AndroidManifest.xml")
    sourceSets["main"].res.srcDirs("src/androidMain/res")
    sourceSets["main"].resources.srcDirs("src/commonMain/composeResources")

    defaultConfig {
        applicationId = "org.example.project"
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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    dependencies {
        debugImplementation(libs.compose.ui.tooling)
    }
}

compose.desktop {
    application {
        mainClass = "MainKt"

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "org.example.project"
            packageVersion = "1.0.0"
        }
    }
}

compose.experimental {
    web.application {}
}
