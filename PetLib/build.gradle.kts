import java.io.File
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "com.movtery.petlib"
    compileSdk = 37

    defaultConfig {
        minSdk = 26
        consumerProguardFiles("proguard-rules.pro")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        compose = true
    }
}

kotlin {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_11)
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.material3)
}

/*
 * Rust 交叉编译：经 cargo-ndk 构建 cdylib 并注入 jniLibs。
 * 需要本机安装 rustup、cargo-ndk（cargo install cargo-ndk），
 * 以及四个 Android target（rustup target add <triple>）。
 */
val rustAbis = mapOf(
    "arm64-v8a" to "aarch64-linux-android",
    "armeabi-v7a" to "armv7-linux-androideabi",
    "x86" to "i686-linux-android",
    "x86_64" to "x86_64-linux-android",
)
val petlibJniLibs = layout.buildDirectory.dir("petlib-jniLibs")


val cargoBuild = tasks.register("cargoBuild") {
    group = "build"
    description = "Builds PetLib Rust core for ARM64 Termux."
    inputs.dir("rust/src")
    inputs.file("rust/Cargo.toml")
    inputs.file("rust/Cargo.lock")
    outputs.dir(petlibJniLibs)

    doLast {
        val rustDir = project.file("rust")
        val output = petlibJniLibs.get().asFile
        val abiDir = File(output, "arm64-v8a")
        abiDir.mkdirs()

        val command = listOf(
            "cargo", "build",
            "--target", "aarch64-linux-android",
            "--release",
            "-j", "1"
        )

        val process = ProcessBuilder(command)
            .directory(rustDir)
            .redirectErrorStream(true)
            .start()

        val log = process.inputStream.bufferedReader().readText()
        val exit = process.waitFor()
        println(log)

        if (exit != 0) {
            throw GradleException("PetLib Rust build failed:\n$log")
        }

        val library = File(rustDir, "target/aarch64-linux-android/release/libpetlib.so")
        if (!library.isFile) {
            throw GradleException("libpetlib.so was not generated: ${library.absolutePath}")
        }

        library.copyTo(File(abiDir, "libpetlib.so"), overwrite = true)
        println("PetLib ARM64 library copied to ${abiDir.absolutePath}")
    }
}


androidComponents {
    onVariants { variant ->
        // cargo-ndk 产物目录（build/petlib-jniLibs/<abi>/libpetlib.so）。
        variant.sources.jniLibs?.addStaticSourceDirectory(
            petlibJniLibs.get().asFile.apply { mkdirs() }.absolutePath
        )
    }
}

tasks.named("preBuild") {
    dependsOn(cargoBuild)
}
