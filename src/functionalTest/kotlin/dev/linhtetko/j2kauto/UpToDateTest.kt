package dev.linhtetko.j2kauto

import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File
import kotlin.test.assertEquals

class UpToDateTest {

    @TempDir
    lateinit var projectDir: File

    private fun run(): TaskOutcome? =
        GradleRunner.create()
            .withProjectDir(projectDir)
            .withArguments("generateJsonModels")
            .build()
            .task(":generateJsonModels")
            ?.outcome

    @Test
    fun `second run is up-to-date and edits retrigger generation`() {
        TestProjects.jvmProject(projectDir)

        assertEquals(TaskOutcome.SUCCESS, run())
        assertEquals(TaskOutcome.UP_TO_DATE, run())

        projectDir.resolve("src/main/json/user.json")
            .writeText("""{"id": 2, "user_name": "changed"}""")
        assertEquals(TaskOutcome.SUCCESS, run())
        assertEquals(TaskOutcome.UP_TO_DATE, run())
    }
}
