package dev.linhtetko.j2kauto

import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CachingAndConfigCacheTest {

    @TempDir
    lateinit var workDir: File

    @Test
    fun `task output is relocatable via the build cache`() {
        val cacheDir = workDir.resolve("build-cache")
        val projectA = workDir.resolve("project-a").apply { mkdirs() }
        val projectB = workDir.resolve("project-b").apply { mkdirs() }

        for (dir in listOf(projectA, projectB)) {
            TestProjects.jvmProject(dir)
            dir.resolve("settings.gradle.kts").appendText(
                "\nbuildCache { local { directory = file(\"${cacheDir.absolutePath}\") } }\n",
            )
        }

        val first = GradleRunner.create()
            .withProjectDir(projectA)
            .withArguments("generateJsonModels", "--build-cache")
            .build()
        assertEquals(TaskOutcome.SUCCESS, first.task(":generateJsonModels")?.outcome)

        val relocated = GradleRunner.create()
            .withProjectDir(projectB)
            .withArguments("generateJsonModels", "--build-cache")
            .build()
        assertEquals(TaskOutcome.FROM_CACHE, relocated.task(":generateJsonModels")?.outcome)
    }

    @Test
    fun `configuration cache entry is stored and reused with zero problems`() {
        val projectDir = workDir.resolve("cc-project").apply { mkdirs() }
        TestProjects.jvmProject(projectDir)

        val first = GradleRunner.create()
            .withProjectDir(projectDir)
            .withArguments("generateJsonModels", "--configuration-cache")
            .build()
        assertTrue("Configuration cache entry stored" in first.output, first.output)

        val second = GradleRunner.create()
            .withProjectDir(projectDir)
            .withArguments("generateJsonModels", "--configuration-cache")
            .build()
        assertTrue("Reusing configuration cache" in second.output, second.output)
    }
}
