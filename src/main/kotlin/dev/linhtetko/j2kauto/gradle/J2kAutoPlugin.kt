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

        extension.targets.all { target ->
            target.packageName.convention(extension.packageName)
            target.annotationStyle.convention(extension.annotationStyle)
            target.visibility.convention(extension.visibility)
            target.useVar.convention(extension.useVar)
            target.defaultsForNullable.convention(extension.defaultsForNullable)
            target.alwaysAnnotate.convention(extension.alwaysAnnotate)
            target.rootClassNames.convention(extension.rootClassNames)
        }

        // Each ecosystem registers its own task(s); AndroidWiring/KotlinJvmWiring are the
        // only classes importing AGP/KGP types, and they're loaded only inside these
        // callbacks — so a consumer without AGP (or KGP) on the classpath never fails.
        project.plugins.withId("com.android.base") {
            AndroidWiring.wire(project, extension)
        }
        project.plugins.withId("org.jetbrains.kotlin.jvm") {
            KotlinJvmWiring.wire(project, extension)
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
            config: J2kAutoConfig,
            sources: Provider<Any>,
        ) {
            task.description = "Generates Kotlin data classes from JSON samples."
            task.group = "j2k-auto"
            task.sourceFiles.from(sources)
            task.packageName.set(config.packageName)
            task.annotationStyle.set(config.annotationStyle)
            task.visibility.set(config.visibility)
            task.useVar.set(config.useVar)
            task.defaultsForNullable.set(config.defaultsForNullable)
            task.alwaysAnnotate.set(config.alwaysAnnotate)
            task.rootClassNames.set(config.rootClassNames)
        }

        fun registerTask(
            project: Project,
            name: String,
            config: J2kAutoConfig,
            sources: Provider<Any>,
        ): TaskProvider<GenerateKotlinFromJsonTask> =
            project.tasks.register(name, GenerateKotlinFromJsonTask::class.java) { task ->
                configureCommon(task, config, sources)
                task.outputDir.convention(project.layout.buildDirectory.dir("generated/j2kauto/$name"))
            }
    }
}
