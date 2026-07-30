package dev.linhtetko.j2kauto.gradle

import dev.linhtetko.j2kauto.AnnotationStyle
import dev.linhtetko.j2kauto.Visibility
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.TaskProvider

class J2kAutoPlugin : Plugin<Project> {

    override fun apply(project: Project) {
        val extension = project.extensions.create("j2kAuto", J2kAutoExtension::class.java)
        extension.packageName.convention("generated.j2kauto")
        extension.annotationStyle.convention(AnnotationStyle.KOTLINX)
        extension.visibility.convention(Visibility.PUBLIC)
        extension.useVar.convention(false)
        extension.defaultsForNullable.convention(true)
        extension.alwaysAnnotate.convention(false)

        // Lazily fall back to src/main/json when the build script never calls source(...).
        val defaultSourceDir = project.layout.projectDirectory.dir("src/main/json")
        val effectiveSources: Provider<Any> = project.provider {
            if (extension.sources.from.isEmpty()) defaultSourceDir else extension.sources
        }

        // Each ecosystem registers its own task(s); AndroidWiring/KotlinJvmWiring are the
        // only classes importing AGP/KGP types, and they're loaded only inside these
        // callbacks — so a consumer without AGP (or KGP) on the classpath never fails.
        project.plugins.withId("com.android.base") {
            AndroidWiring.wire(project, extension, effectiveSources)
        }
        project.plugins.withId("org.jetbrains.kotlin.jvm") {
            KotlinJvmWiring.wire(project, extension, effectiveSources)
        }
        project.plugins.withId("org.jetbrains.kotlin.multiplatform") {
            project.logger.warn(
                "j2k-auto: Kotlin Multiplatform is not supported yet — no sources will be generated for KMP source sets.",
            )
        }
    }

    internal companion object {
        fun configureCommon(
            task: GenerateKotlinFromJsonTask,
            extension: J2kAutoExtension,
            sources: Provider<Any>,
        ) {
            task.description = "Generates Kotlin data classes from JSON samples."
            task.group = "j2k-auto"
            task.sourceFiles.from(sources)
            task.packageName.set(extension.packageName)
            task.annotationStyle.set(extension.annotationStyle)
            task.visibility.set(extension.visibility)
            task.useVar.set(extension.useVar)
            task.defaultsForNullable.set(extension.defaultsForNullable)
            task.alwaysAnnotate.set(extension.alwaysAnnotate)
            task.rootClassNames.set(extension.rootClassNames)
        }

        fun registerTask(
            project: Project,
            name: String,
            extension: J2kAutoExtension,
            sources: Provider<Any>,
        ): TaskProvider<GenerateKotlinFromJsonTask> =
            project.tasks.register(name, GenerateKotlinFromJsonTask::class.java) { task ->
                configureCommon(task, extension, sources)
                task.outputDir.convention(project.layout.buildDirectory.dir("generated/j2kauto/$name"))
            }
    }
}
