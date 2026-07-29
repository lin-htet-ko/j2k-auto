package dev.linhtetko.j2kauto

import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome
import org.junit.jupiter.api.Assumptions.assumeTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AndroidFunctionalTest {

    @TempDir
    lateinit var projectDir: File

    @Test
    fun `generates per-variant tasks and compiles into an android app`() {
        val sdk = TestProjects.androidSdkDir()
        assumeTrue(sdk != null, "Android SDK not found — skipping Android functional test")

        TestProjects.androidProject(projectDir, sdk!!)

        val result = GradleRunner.create()
            .withProjectDir(projectDir)
            .withArguments("compileDebugKotlin", "--stacktrace")
            .build()

        assertEquals(TaskOutcome.SUCCESS, result.task(":generateJsonModelsDebug")?.outcome)
        assertEquals(TaskOutcome.SUCCESS, result.task(":compileDebugKotlin")?.outcome)

        // AGP owns the output location for variant-wired generated sources.
        val generated = projectDir.resolve(
            "build/generated/kotlin/generateJsonModelsDebug/test/model/User.kt",
        )
        assertTrue(generated.isFile, "expected generated file at $generated")
    }
}
