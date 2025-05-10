plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.compose)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.serialization)
}

kotlin {
    jvmToolchain(libs.versions.jvm.get().toInt())

    androidTarget()
    iosArm64 {
        binaries.framework {
            baseName = "ComposeApp"
            binaryOption("bundleId", "com.juul.sensortag.ios")
            binaryOption("bundleShortVersionString", "0.0.1")
            binaryOption("bundleVersion", "1")
            export(libs.coroutines)
        }
    }
    js {
        moduleName = "sample"
        browser {
            commonWebpackConfig {
                outputFileName = "sample.js"
            }
        }
        binaries.executable()
    }

    jvm()

    listOf(
        macosX64(),
        macosArm64(),
    ).forEach { target ->
        target.binaries.executable {
            baseName = "sensortag"
            entryPoint = "com.juul.sensortag.main"
        }
    }

    applyDefaultHierarchyTemplate()

    sourceSets {
        all {
            languageSettings.optIn("com.juul.kable.ExperimentalApi")
            languageSettings.optIn("kotlin.uuid.ExperimentalUuidApi")
        }

        val composeMain by creating {
            dependsOn(commonMain.get())
        }
        androidMain.get().dependsOn(composeMain)
        iosMain.get().dependsOn(composeMain)
        jsMain.get().dependsOn(composeMain)
        jvmMain.get().dependsOn(composeMain)

        val notJsMain by creating {
            dependsOn(commonMain.get())
        }
        androidMain.get().dependsOn(notJsMain)
        appleMain.get().dependsOn(notJsMain)
        jvmMain.get().dependsOn(notJsMain)

        commonMain.dependencies {
            api(libs.coroutines)
            implementation(compose.foundation)
            implementation(compose.material)
            implementation(compose.runtime)
            implementation(compose.ui)
            implementation(libs.bundles.krayon)
            implementation(libs.bundles.voyager)
            implementation(libs.datetime)
            implementation(libs.kable)
            implementation(libs.khronicle)
            implementation(libs.serialization)
            implementation(projects.bluetooth)
        }

        androidMain.dependencies {
            implementation(libs.compose.activity)
            implementation(libs.kable.permissions)
        }

        composeMain.dependencies {
            implementation(libs.krayon.compose)
        }

        jvmMain.dependencies {
            implementation(compose.desktop.currentOs)
            implementation(libs.coroutines.swing)
        }

        notJsMain.dependencies {
            implementation(libs.androidx.lifecycle)
        }
    }
}

android {
    namespace = "com.juul.sensortag"
    compileSdk = libs.versions.android.compile.get().toInt()

    sourceSets["main"].manifest.srcFile("src/androidMain/AndroidManifest.xml")
    sourceSets["main"].res.srcDirs("src/androidMain/res")
    sourceSets["main"].resources.srcDirs("src/commonMain/resources")

    defaultConfig {
        applicationId = "com.juul.sensortag.android"
        minSdk = libs.versions.android.min.get().toInt()
        targetSdk = libs.versions.android.target.get().toInt()
        versionCode = 1
        versionName = "1.0"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    buildFeatures.compose = true

    // Provides `java.time` for kotlinx.datetime on API < 26.
    // https://github.com/Kotlin/kotlinx-datetime?tab=readme-ov-file#using-in-your-projects
    compileOptions.isCoreLibraryDesugaringEnabled = true
}

dependencies {
    // Provides `java.time` for kotlinx.datetime on Android API < 26.
    // https://github.com/Kotlin/kotlinx-datetime?tab=readme-ov-file#using-in-your-projects
    coreLibraryDesugaring(libs.desugar)
}

compose {
    desktop {
        application {
            mainClass = "com.juul.sensortag.MainKt"
        }
    }
}
