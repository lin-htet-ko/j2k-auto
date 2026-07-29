package dev.linhtetko.j2kauto

import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class JvmFunctionalTest {

    @TempDir
    lateinit var projectDir: File

    private fun runner(vararg args: String): GradleRunner =
        GradleRunner.create()
            .withProjectDir(projectDir)
            .withArguments(*args, "--stacktrace")

    @Test
    fun `generates and compiles kotlin from json in a jvm project`() {
        TestProjects.jvmProject(projectDir)

        val result = runner("compileKotlin").build()

        assertEquals(TaskOutcome.SUCCESS, result.task(":generateJsonModels")?.outcome)
        assertEquals(TaskOutcome.SUCCESS, result.task(":compileKotlin")?.outcome)

        val generated = projectDir.resolve(
            "build/generated/j2kauto/generateJsonModels/test/model/User.kt",
        )
        assertTrue(generated.isFile, "expected generated file at $generated")
        val text = generated.readText()
        assertTrue("@Serializable" in text)
        assertTrue("data class User(" in text)
        assertTrue("data class Order(" in text)
        assertTrue("val note: String? = null" in text)
    }

    @Test
    fun `rootClassName override and annotation style flow through the DSL`() {
        TestProjects.jvmProject(
            projectDir,
            extraExtensionConfig = """
                annotationStyle = dev.linhtetko.j2kauto.AnnotationStyle.NONE
                rootClassName("user.json", "User")
            """.trimIndent(),
        )

        val result = runner("generateJsonModels").build()

        assertEquals(TaskOutcome.SUCCESS, result.task(":generateJsonModels")?.outcome)
        val text = projectDir
            .resolve("build/generated/j2kauto/generateJsonModels/test/model/User.kt")
            .readText()
        assertTrue("@Serializable" !in text, "NONE style must not emit kotlinx annotations")
    }
}
