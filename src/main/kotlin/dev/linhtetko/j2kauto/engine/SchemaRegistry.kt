package dev.linhtetko.j2kauto.engine

import dev.linhtetko.j2kauto.engine.InferredType.ArrayType
import dev.linhtetko.j2kauto.engine.InferredType.ObjectType

/** One generated class: its final name plus the (canonical) shape it represents. */
data class NamedClass(val name: String, val type: ObjectType)

/** The classes first introduced by a single JSON input file. */
data class FileSchemas(
    val fileName: String,
    val rootClassName: String,
    val classes: List<NamedClass>,
)

/**
 * Assigns collision-free class names to canonical object shapes. One instance
 * is shared across ALL input files: a shape already registered by an earlier
 * file (see [ShapeUnifier]) is reused, so the same JSON structure in several
 * files yields exactly one class.
 */
class SchemaRegistry {

    private val nameByShape = LinkedHashMap<ObjectType, String>()
    private val usedNames = mutableSetOf<String>()
    private val ordered = mutableListOf<NamedClass>()

    /** All classes registered so far, in emission order. */
    val allClasses: List<NamedClass> get() = ordered.toList()

    /**
     * Registers one file's canonical root and returns only the classes FIRST
     * seen in this call — empty when the whole file deduped into earlier files.
     */
    fun register(root: ObjectType, rootClassName: String): List<NamedClass> {
        val before = ordered.size
        walkObject(root, hint = rootClassName, parentName = null)
        return ordered.subList(before, ordered.size).toList()
    }

    private fun walkObject(obj: ObjectType, hint: String, parentName: String?): String {
        val shape = structuralKey(obj)
        nameByShape[shape]?.let { return it }

        val name = allocate(hint.ifEmpty { "Class" }, parentName)
        nameByShape[shape] = name
        usedNames += name
        ordered += NamedClass(name, shape)

        for ((fieldKey, field) in obj.fields) {
            walkType(field.type, hint = Names.className(fieldKey), parentName = name)
        }
        return name
    }

    private fun walkType(type: InferredType, hint: String, parentName: String) {
        when (type) {
            // Empty objects carry no information and are emitted as the strategy's
            // "any" type — never as a (invalid) parameterless data class.
            is ObjectType -> if (type.fields.isNotEmpty()) walkObject(type, hint, parentName)
            is ArrayType -> walkType(type.element, Names.singularize(hint), parentName)
            else -> Unit
        }
    }

    /** First-seen hint wins; collisions get parent-prefixed, then numeric suffixes. */
    private fun allocate(hint: String, parentName: String?): String {
        if (hint !in usedNames) return hint
        if (parentName != null && parentName + hint !in usedNames) return parentName + hint
        var i = 2
        while ("$hint$i" in usedNames) i++
        return "$hint$i"
    }

    companion object {
        /** Shape identity ignores the object's own nullability; field nullability is retained. */
        fun structuralKey(obj: ObjectType): ObjectType = obj.copy(nullable = false)
    }
}
