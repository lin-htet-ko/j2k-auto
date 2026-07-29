package dev.linhtetko.j2kauto.codegen

import com.squareup.kotlinpoet.FileSpec
import dev.linhtetko.j2kauto.AnnotationStyle
import dev.linhtetko.j2kauto.engine.FileSchemas
import dev.linhtetko.j2kauto.engine.Names
import dev.linhtetko.j2kauto.engine.SchemaInferrer
import dev.linhtetko.j2kauto.engine.SchemaRegistry
import dev.linhtetko.j2kauto.engine.ShapeUnifier

/** One JSON sample: the file name drives the root class name. */
data class JsonInput(val fileName: String, val text: String)

/**
 * The full text-to-FileSpec pipeline shared by the Gradle task and tests:
 * parse → infer → unify compatible shapes across ALL files → one global
 * registry (dedupe + naming) → KotlinPoet emission per file. A file whose
 * shapes all deduped into earlier files emits nothing.
 */
object J2kPipeline {

    fun generate(
        inputs: List<JsonInput>,
        packageName: String,
        style: AnnotationStyle,
        options: CodegenOptions = CodegenOptions(),
        rootClassNames: Map<String, String> = emptyMap(),
    ): List<FileSpec> {
        val strategy = AnnotationStrategy.of(style)
        val parsed = inputs
            .sortedBy { it.fileName } // deterministic across file systems
            .map { input ->
                val rootName = rootClassNames[input.fileName]
                    ?: Names.className(input.fileName.removeSuffix(".json")).ifEmpty { "Root" }
                Triple(input.fileName, rootName, SchemaInferrer.inferRoot(input.text, input.fileName))
            }

        val unification = ShapeUnifier.unify(parsed.map { it.third })
        val registry = SchemaRegistry()
        val files = parsed.mapNotNull { (fileName, rootName, root) ->
            val delta = registry.register(unification.canonicalOf(root), rootName)
            if (delta.isEmpty()) null else FileSchemas(fileName, rootName, delta)
        }

        val nameByShape = registry.allClasses.associate { it.type to it.name }
        return files.map { file ->
            KotlinFileGenerator.generate(file, packageName, strategy, options, nameByShape)
        }
    }
}
