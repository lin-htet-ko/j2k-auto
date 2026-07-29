package dev.linhtetko.j2kauto.gradle

import com.android.build.api.variant.AndroidComponentsExtension
import org.gradle.api.Project
import org.gradle.api.provider.Provider

/**
 * Android wiring. The only class referencing AGP types (compileOnly) — it is
 * class-loaded strictly inside `plugins.withId("com.android.base")`.
 *
 * One task per variant: `addGeneratedSourceDirectory` rebinds the task's
 * outputDir to a variant-scoped location, so a single shared task would
 * cross-talk between variants. Identical inputs across variants resolve to
 * build-cache hits, so the duplicate work is only on cold caches.
 */
internal object AndroidWiring {

    fun wire(project: Project, extension: J2kAutoExtension, sources: Provider<Any>) {
        val components = project.extensions.getByType(AndroidComponentsExtension::class.java)
        components.onVariants(components.selector().all()) { variant ->
            val taskName = "generateJsonModels" + variant.name.replaceFirstChar { it.uppercaseChar() }
            val task = J2kAutoPlugin.registerTask(project, taskName, extension, sources)

            val target = variant.sources.kotlin ?: variant.sources.java
            if (target == null) {
                project.logger.warn(
                    "j2k-auto: variant ${variant.name} exposes no kotlin/java sources; skipping generation.",
                )
                return@onVariants
            }
            target.addGeneratedSourceDirectory(task, GenerateKotlinFromJsonTask::outputDir)
        }
    }
}
