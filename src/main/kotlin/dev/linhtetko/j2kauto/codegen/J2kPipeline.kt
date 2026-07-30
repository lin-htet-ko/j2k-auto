package dev.linhtetko.j2kauto.codegen

import com.squareup.kotlinpoet.FileSpec
import dev.linhtetko.j2kauto.AnnotationStyle
import dev.linhtetko.j2kauto.engine.FileSchemas
import dev.linhtetko.j2kauto.engine.InferredType.ObjectType
import dev.linhtetko.j2kauto.engine.Names
import dev.linhtetko.j2kauto.engine.SchemaInferrer
import dev.linhtetko.j2kauto.engine.SchemaRegistry
import dev.linhtetko.j2kauto.engine.ShapeUnifier

/** One JSON sample: the relative path drives the root class name and subpackage. */
data class JsonInput(val relativePath: String, val text: String)

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
            .sortedBy { it.relativePath } // deterministic across file systems
            .map { input ->
                val fileName = input.relativePath.split('/', '\\').last()
                val rootName = rootClassNames[input.relativePath]
                    ?: Names.className(fileName.removeSuffix(".json")).ifEmpty { "Root" }
                val subPackagePath = input.relativePath.replace('\\', '/').substringBeforeLast('/', "")
                val fullPackage = packageName + Names.subpackageName(subPackagePath)

                ParsedInput(
                    input.relativePath,
                    rootName,
                    fullPackage,
                    SchemaInferrer.inferRoot(input.text, input.relativePath),
                )
            }

        val unification = ShapeUnifier.unify(parsed.map { it.root })
        val registry = SchemaRegistry()
        val files = parsed.mapNotNull { p ->
            val delta = registry.register(unification.canonicalOf(p.root), p.rootName, p.packageName)
            if (delta.isEmpty()) null else FileSchemas(p.relativePath, p.rootName, p.packageName, delta)
        }

        val classByShape = registry.allClasses.associateBy { SchemaRegistry.structuralKey(it.type) }
        return files.map { file ->
            KotlinFileGenerator.generate(file, strategy, options, classByShape)
        }
    }

    private data class ParsedInput(
        val relativePath: String,
        val rootName: String,
        val packageName: String,
        val root: ObjectType,
    )
}
