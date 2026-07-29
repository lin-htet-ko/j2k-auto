package dev.linhtetko.j2kauto.gradle

import org.gradle.api.Project
import org.gradle.api.provider.Provider
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmProjectExtension

/**
 * Kotlin/JVM wiring. The only class referencing KGP types (compileOnly) — it is
 * class-loaded strictly inside `plugins.withId("org.jetbrains.kotlin.jvm")`.
 */
internal object KotlinJvmWiring {

    fun wire(project: Project, extension: J2kAutoExtension, sources: Provider<Any>) {
        val task = J2kAutoPlugin.registerTask(project, "generateJsonModels", extension, sources)

        val kotlin = project.extensions.getByType(KotlinJvmProjectExtension::class.java)
        kotlin.sourceSets.named("main") { sourceSet ->
            // srcDir with a task-derived provider carries the task dependency automatically.
            sourceSet.kotlin.srcDir(task.flatMap { it.outputDir })
        }
    }
}
