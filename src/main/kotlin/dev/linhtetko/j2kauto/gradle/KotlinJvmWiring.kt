package dev.linhtetko.j2kauto.gradle

import org.gradle.api.Project
import org.gradle.api.provider.Provider
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmProjectExtension

/**
 * Kotlin/JVM wiring. The only class referencing KGP types (compileOnly) — it is
 * class-loaded strictly inside `plugins.withId("org.jetbrains.kotlin.jvm")`.
 */
internal object KotlinJvmWiring {

    fun wire(project: Project, extension: J2kAutoExtension) {
        val kotlin = project.extensions.getByType(KotlinJvmProjectExtension::class.java)
        kotlin.sourceSets.named("main") { sourceSet ->
            // 1. The default target
            wireTarget(project, extension, "generateJsonModels", sourceSet, extension)

            // 2. Named targets
            extension.targets.all { target ->
                val targetName = target.name.replaceFirstChar { it.uppercaseChar() }
                wireTarget(project, target, "generateJsonModels$targetName", sourceSet, extension)
            }
        }
    }

    private fun wireTarget(
        project: Project,
        config: J2kAutoConfig,
        taskName: String,
        sourceSet: org.jetbrains.kotlin.gradle.plugin.KotlinSourceSet,
        extension: J2kAutoExtension
    ) {
        val defaultSourceDir = project.layout.projectDirectory.dir("src/main/json")
        val effectiveSources: Provider<Any> = project.provider {
            val isDefault = config === extension
            val hasExplicitSource = config.sources.from.isNotEmpty()
            val hasOtherTargets = extension.targets.isNotEmpty()

            if (hasExplicitSource) {
                config.sources
            } else if (isDefault && hasOtherTargets) {
                // If this is the default target and other targets are defined,
                // skip unless source(...) was explicitly called at the top level.
                project.files()
            } else {
                defaultSourceDir
            }
        }

        val task = J2kAutoPlugin.registerTask(project, taskName, config, effectiveSources)
        sourceSet.kotlin.srcDir(task.flatMap { it.outputDir })
    }
}
