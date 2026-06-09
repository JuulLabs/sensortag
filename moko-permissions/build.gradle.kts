plugins {
    id("com.android.library")
    kotlin("multiplatform")
}

kotlin {
    jvmToolchain(libs.versions.jvm.get().toInt())

    androidTarget()
    iosArm64()
    iosSimulatorArm64()
    js().browser()
    jvm()
    macosArm64()

    applyDefaultHierarchyTemplate()

    sourceSets {
        val nopMain by creating {
            dependsOn(commonMain.get())
        }
        macosMain.get().dependsOn(nopMain)
        jsMain.get().dependsOn(nopMain)
        jvmMain.get().dependsOn(nopMain)

        commonMain.dependencies {
            implementation(libs.coroutines)
        }

        androidMain.dependencies {
            implementation(libs.compose.activity)
            implementation(libs.androidx.lifecycle)
        }
    }
}

android {
    namespace = "dev.icerock.moko.permissions"
    compileSdk = libs.versions.android.compile.get().toInt()
    defaultConfig.minSdk = libs.versions.android.min.get().toInt()
}
