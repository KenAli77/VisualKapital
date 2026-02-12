import java.util.Properties
import com.codingfeline.buildkonfig.compiler.FieldSpec.Type.STRING
import com.codingfeline.buildkonfig.compiler.FieldSpec.Type.BOOLEAN
import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.kotlinSerialization)
    alias(libs.plugins.buildkonfig)
    alias(libs.plugins.ksp)
    alias(libs.plugins.androidx.room)
}

// Read from local.properties
val localProperties = Properties()
val localPropertiesFile = rootProject.file("local.properties")
if (localPropertiesFile.exists()) {
    localPropertiesFile.inputStream().use { localProperties.load(it) }
}

fun getSecret(key: String): String = localProperties.getProperty(key) ?: ""

kotlin {
    androidTarget {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_11)
        }
    }
    
    listOf(
        iosArm64(),
        iosX64(),
        iosSimulatorArm64(),
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "ComposeApp"
            isStatic = true
        }
    }
//    project.afterEvaluate {
//        tasks.named("kspKotlinIosArm64") {
//            dependsOn(tasks.named("generateResourceAccessorsForIosArm64Main"))
//            enabled = false
//        }
//    }


    sourceSets {
        androidMain.dependencies {
            implementation(libs.compose.uiToolingPreview)
            implementation(libs.androidx.activity.compose)
            implementation(libs.koin.android)
            implementation(libs.ktor.client.okhttp)
            implementation(libs.purchases)
            implementation(libs.purchases.ui)
        }
        iosMain.dependencies {
            implementation(libs.ktor.client.darwin)
        }
        commonMain.dependencies {
            implementation(libs.compose.runtime)
            implementation(libs.compose.foundation)
            implementation(libs.compose.material3)
            implementation(libs.material.icons.extended)
            implementation(libs.compose.ui)
            implementation(libs.compose.components.resources)
            implementation(libs.navigation.compose)
            implementation(libs.compose.uiToolingPreview)
            implementation(libs.androidx.lifecycle.viewmodelCompose)
            implementation(libs.androidx.lifecycle.runtimeCompose)
            implementation(libs.koin.core)
            implementation(libs.koin.compose)
            implementation(libs.koin.compose)
            implementation(libs.koin.compose.viewmodel)
            implementation(libs.bundles.ktor)
            implementation(libs.coil.compose)
            implementation(libs.coil.network.ktor)
            implementation(libs.kotlinx.datetime)
            implementation(libs.androidx.room.runtime)
            implementation(libs.androidx.sqlite.bundled)

            implementation(libs.datetime.wheel.picker)
            implementation(libs.purchases.kmp.core)

        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
    }

}

android {
    namespace = "com.visualmoney.app"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    defaultConfig {
        applicationId = "com.visualmoney.app"
        minSdk = libs.versions.android.minSdk.get().toInt()
        targetSdk = libs.versions.android.targetSdk.get().toInt()
        versionCode = 1
        versionName = "1.0"
    }

    signingConfigs {
        create("release") {
            val path = getSecret("KEYSTORE_PATH")
            storeFile = file(path)
            storePassword = getSecret("KEYSTORE_PASSWORD")
            keyAlias = getSecret("KEY_ALIAS")
            keyPassword = getSecret("KEYSTORE_PASSWORD")
        }
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = signingConfigs.getByName("release")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    debugImplementation(libs.compose.uiTooling)
    add("kspAndroid", libs.androidx.room.compiler)
    add("kspIosSimulatorArm64", libs.androidx.room.compiler)
    add("kspIosX64", libs.androidx.room.compiler)
    add("kspIosArm64", libs.androidx.room.compiler)
}

buildkonfig {
    packageName = "com.visualmoney.app"

    defaultConfigs {
        val fmpKey = getSecret("FMP_API_KEY").ifEmpty { "invalid" }
        val logoKey = getSecret("LOGO_DEV_KEY").ifEmpty { "invalid" }
        val rcIosKey = getSecret("RC_API_KEY_IOS").ifEmpty { "invalid" }
        val rcAndroidKey = getSecret("RC_API_KEY_ANDROID").ifEmpty { "invalid" }

        buildConfigField(BOOLEAN, "DEBUG", "false")
        buildConfigField(STRING, "RC_API_KEY_ANDROID", rcAndroidKey)
        buildConfigField(STRING, "FMP_API_KEY", fmpKey)
        buildConfigField(STRING, "LOGO_DEV_KEY", logoKey)
        buildConfigField(STRING, "RC_API_KEY_IOS", rcIosKey)
    }
    
    targetConfigs {
        create("androidDebug") {
            val rcAndroidKey = getSecret("RC_API_KEY_TEST_ANDROID").ifEmpty { getSecret("RC_API_KEY_ANDROID") }.ifEmpty { "invalid" }
            buildConfigField(STRING, "RC_API_KEY_ANDROID", rcAndroidKey)
            buildConfigField(BOOLEAN, "DEBUG", "true")
        }
    }
}

room {
    schemaDirectory("$projectDir/schemas")
}
