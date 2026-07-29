package dev.linhtetko.j2kauto

import java.io.File

/** Scaffolds throwaway consumer projects for TestKit runs. */
object TestProjects {

    private val pluginRepo: String = System.getProperty("pluginRepo")
    private val pluginVersion: String = System.getProperty("pluginVersion")

    const val KOTLIN_VERSION = "2.2.20"
    const val AGP_VERSION = "9.2.1"

    fun settings(rootName: String): String = """
        pluginManagement {
            repositories {
                maven { url = uri("${File(pluginRepo).toURI()}") }
                google()
                mavenCentral()
                gradlePluginPortal()
            }
        }
        dependencyResolutionManagement {
            repositories {
                google()
                mavenCentral()
            }
        }
        rootProject.name = "$rootName"
    """.trimIndent()

    fun jvmBuildScript(extraExtensionConfig: String = ""): String = """
        plugins {
            id("org.jetbrains.kotlin.jvm") version "$KOTLIN_VERSION"
            id("org.jetbrains.kotlin.plugin.serialization") version "$KOTLIN_VERSION"
            id("dev.linhtetko.j2k-auto") version "$pluginVersion"
        }
        dependencies {
            implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.9.0")
        }
        j2kAuto {
            packageName = "test.model"
            $extraExtensionConfig
        }
    """.trimIndent()

    fun androidBuildScript(): String = """
        plugins {
            id("com.android.application") version "$AGP_VERSION"
            id("org.jetbrains.kotlin.plugin.serialization") version "2.4.0"
            id("dev.linhtetko.j2k-auto") version "$pluginVersion"
        }
        android {
            namespace = "test.app"
            compileSdk = 36
            defaultConfig {
                minSdk = 24
            }
        }
        dependencies {
            implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.9.0")
        }
        j2kAuto {
            packageName = "test.model"
        }
    """.trimIndent()

    val userJson: String = """
        {
          "id": 1,
          "user_name": "lin",
          "orders": [
            {"order_id": 1, "note": "x"},
            {"order_id": 2}
          ]
        }
    """.trimIndent()

    /** Writes a ready-to-build Kotlin/JVM consumer into [dir]. */
    fun jvmProject(dir: File, extraExtensionConfig: String = "") {
        dir.resolve("settings.gradle.kts").writeText(settings(dir.name))
        dir.resolve("build.gradle.kts").writeText(jvmBuildScript(extraExtensionConfig))
        val json = dir.resolve("src/main/json").apply { mkdirs() }
        json.resolve("user.json").writeText(userJson)
        val kotlin = dir.resolve("src/main/kotlin").apply { mkdirs() }
        kotlin.resolve("Consumer.kt").writeText(
            """
            import test.model.User

            fun describe(user: User): String = user.userName + user.orders.size
            """.trimIndent(),
        )
    }

    /** Returns the Android SDK dir, or null when unavailable (test should be skipped). */
    fun androidSdkDir(): File? {
        System.getenv("ANDROID_HOME")?.let { path ->
            File(path).takeIf { it.isDirectory }?.let { return it }
        }
        val userHome = File(System.getProperty("user.home"))
        return userHome.resolve("Library/Android/sdk").takeIf { it.isDirectory }
    }

    /** Writes a ready-to-build Android application consumer into [dir]. */
    fun androidProject(dir: File, sdkDir: File) {
        dir.resolve("settings.gradle.kts").writeText(settings(dir.name))
        dir.resolve("build.gradle.kts").writeText(androidBuildScript())
        dir.resolve("gradle.properties").writeText("android.useAndroidX=true\n")
        dir.resolve("local.properties").writeText("sdk.dir=${sdkDir.absolutePath}\n")
        val main = dir.resolve("src/main").apply { mkdirs() }
        main.resolve("AndroidManifest.xml").writeText("<manifest />\n")
        main.resolve("json").apply { mkdirs() }.resolve("user.json").writeText(userJson)
        main.resolve("kotlin").apply { mkdirs() }.resolve("Consumer.kt").writeText(
            """
            import test.model.User

            fun describe(user: User): String = user.userName + user.orders.size
            """.trimIndent(),
        )
    }
}
