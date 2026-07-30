package dev.linhtetko.j2kauto

import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class MultiTargetFunctionalTest {

    @TempDir
    lateinit var projectDir: File

    private fun runner(vararg args: String): GradleRunner =
        GradleRunner.create()
            .withProjectDir(projectDir)
            .withArguments(*args, "--stacktrace")

    @Test
    fun `default target is skipped when named targets are present and no explicit top-level source is set`() {
        projectDir.resolve("settings.gradle.kts").writeText(TestProjects.settings("multi-target-skip-default"))
        projectDir.resolve("build.gradle.kts").writeText(
            """
            plugins {
                id("org.jetbrains.kotlin.jvm") version "${TestProjects.KOTLIN_VERSION}"
                id("org.jetbrains.kotlin.plugin.serialization") version "${TestProjects.KOTLIN_VERSION}"
                id("io.github.lin-htet-ko.j2k-auto") version "${System.getProperty("pluginVersion")}"
            }
            dependencies {
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.9.0")
            }
            j2kAuto {
                packageName = "default.model"
                // No source(...) call here, and targets are present -> default target should be skipped
                
                targets {
                    register("api") {
                        packageName = "api.model"
                        source(layout.projectDirectory.dir("src/main/api_json"))
                    }
                }
            }
            """.trimIndent()
        )

        val defaultDir = projectDir.resolve("src/main/json").apply { mkdirs() }
        defaultDir.resolve("default.json").writeText("""{"id": 1}""")

        val apiDir = projectDir.resolve("src/main/api_json").apply { mkdirs() }
        apiDir.resolve("api.json").writeText("""{"status": "ok"}""")

        val kotlinDir = projectDir.resolve("src/main/kotlin").apply { mkdirs() }
        kotlinDir.resolve("Consumer.kt").writeText(
            """
            import api.model.Api

            fun consume() {
                val a = Api(status = "ok")
            }
            """.trimIndent()
        )

        val result = runner("compileKotlin").build()

        val defaultTask = result.task(":generateJsonModels")
        assertTrue(defaultTask == null || defaultTask.outcome == TaskOutcome.NO_SOURCE || defaultTask.outcome == TaskOutcome.SKIPPED, "default task should be skipped or not registered")
        assertEquals(TaskOutcome.SUCCESS, result.task(":generateJsonModelsApi")?.outcome)
        assertEquals(TaskOutcome.SUCCESS, result.task(":compileKotlin")?.outcome)

        assertTrue(!projectDir.resolve("build/generated/j2kauto/generateJsonModels/default/model/Default.kt").exists(), "Default.kt should NOT be generated")
        assertTrue(projectDir.resolve("build/generated/j2kauto/generateJsonModelsApi/api/model/Api.kt").exists(), "Api.kt SHOULD be generated")
    }

    @Test
    fun `default target is included when targets are present if source is explicitly set`() {
        projectDir.resolve("settings.gradle.kts").writeText(TestProjects.settings("multi-target-explicit-default"))
        projectDir.resolve("build.gradle.kts").writeText(
            """
            plugins {
                id("org.jetbrains.kotlin.jvm") version "${TestProjects.KOTLIN_VERSION}"
                id("org.jetbrains.kotlin.plugin.serialization") version "${TestProjects.KOTLIN_VERSION}"
                id("io.github.lin-htet-ko.j2k-auto") version "${System.getProperty("pluginVersion")}"
            }
            dependencies {
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.9.0")
            }
            j2kAuto {
                packageName = "default.model"
                source(layout.projectDirectory.dir("src/main/json")) // Explicit source
                
                targets {
                    register("api") {
                        packageName = "api.model"
                        source(layout.projectDirectory.dir("src/main/api_json"))
                    }
                }
            }
            """.trimIndent()
        )

        val defaultDir = projectDir.resolve("src/main/json").apply { mkdirs() }
        defaultDir.resolve("default.json").writeText("""{"id": 1}""")

        val apiDir = projectDir.resolve("src/main/api_json").apply { mkdirs() }
        apiDir.resolve("api.json").writeText("""{"status": "ok"}""")

        val result = runner("generateJsonModels", "generateJsonModelsApi").build()

        assertEquals(TaskOutcome.SUCCESS, result.task(":generateJsonModels")?.outcome)
        assertEquals(TaskOutcome.SUCCESS, result.task(":generateJsonModelsApi")?.outcome)
    }
}
