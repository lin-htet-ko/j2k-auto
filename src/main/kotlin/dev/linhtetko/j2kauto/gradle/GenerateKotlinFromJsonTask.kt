package dev.linhtetko.j2kauto.gradle

import dev.linhtetko.j2kauto.AnnotationStyle
import dev.linhtetko.j2kauto.Visibility
import dev.linhtetko.j2kauto.codegen.CodegenOptions
import dev.linhtetko.j2kauto.codegen.J2kPipeline
import dev.linhtetko.j2kauto.codegen.JsonInput
import org.gradle.api.DefaultTask
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.MapProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.IgnoreEmptyDirectories
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.SkipWhenEmpty
import org.gradle.api.tasks.TaskAction

/**
 * Generates Kotlin data classes from the `.json` samples in [sourceFiles].
 * Cacheable and configuration-cache safe: all state is Provider-based and the
 * action never touches [org.gradle.api.Project]. Regenerates the whole output
 * directory every run — per-file incrementality would be unsound because of
 * the cross-file class-name collision pass.
 */
@CacheableTask
abstract class GenerateKotlinFromJsonTask : DefaultTask() {

    @get:InputFiles
    @get:PathSensitive(PathSensitivity.RELATIVE)
    @get:SkipWhenEmpty
    @get:IgnoreEmptyDirectories
    abstract val sourceFiles: ConfigurableFileCollection

    @get:Input
    abstract val packageName: Property<String>

    @get:Input
    abstract val annotationStyle: Property<AnnotationStyle>

    @get:Input
    abstract val visibility: Property<Visibility>

    @get:Input
    abstract val useVar: Property<Boolean>

    @get:Input
    abstract val defaultsForNullable: Property<Boolean>

    @get:Input
    abstract val alwaysAnnotate: Property<Boolean>

    @get:Input
    abstract val rootClassNames: MapProperty<String, String>

    @get:OutputDirectory
    abstract val outputDir: DirectoryProperty

    @TaskAction
    fun generate() {
        val out = outputDir.get().asFile
        out.deleteRecursively()
        out.mkdirs()

        val inputs = sourceFiles.asFileTree
            .matching { it.include("**/*.json") }
            .files
            .map { JsonInput(it.name, it.readText()) }
        if (inputs.isEmpty()) return

        val specs = J2kPipeline.generate(
            inputs = inputs,
            packageName = packageName.get(),
            style = annotationStyle.get(),
            options = CodegenOptions(
                useVar = useVar.get(),
                defaultsForNullable = defaultsForNullable.get(),
                alwaysAnnotate = alwaysAnnotate.get(),
                visibility = visibility.get(),
            ),
            rootClassNames = rootClassNames.get(),
        )
        specs.forEach { it.writeTo(out) }
        logger.lifecycle("j2k-auto: generated ${specs.size} file(s) into $out")
    }
}
