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

    fun wire(project: Project, extension: J2kAutoExtension) {
        val components = project.extensions.getByType(AndroidComponentsExtension::class.java)
        components.onVariants(components.selector().all()) { variant ->
            val variantName = variant.name.replaceFirstChar { it.uppercaseChar() }

            // 1. The default target (extension itself)
            wireTarget(project, extension, "generateJsonModels$variantName", variant, extension)

            // 2. Named targets
            extension.targets.all { target ->
                val targetName = target.name.replaceFirstChar { it.uppercaseChar() }
                wireTarget(project, target, "generateJsonModels${targetName}$variantName", variant, extension)
            }
        }
    }

    private fun wireTarget(
        project: Project,
        config: J2kAutoConfig,
        taskName: String,
        variant: com.android.build.api.variant.Variant,
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

        val target = variant.sources.kotlin ?: variant.sources.java
        if (target == null) {
            project.logger.warn(
                "j2k-auto: variant ${variant.name} exposes no kotlin/java sources; skipping generation for $taskName.",
            )
            return
        }
        target.addGeneratedSourceDirectory(task, GenerateKotlinFromJsonTask::outputDir)
    }
}
