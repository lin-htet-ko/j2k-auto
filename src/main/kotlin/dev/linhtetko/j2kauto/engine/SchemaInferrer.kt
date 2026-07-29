package dev.linhtetko.j2kauto.engine

import dev.linhtetko.j2kauto.engine.InferredType.ArrayType
import dev.linhtetko.j2kauto.engine.InferredType.Field
import dev.linhtetko.j2kauto.engine.InferredType.JsonAny
import dev.linhtetko.j2kauto.engine.InferredType.NullType
import dev.linhtetko.j2kauto.engine.InferredType.ObjectType
import dev.linhtetko.j2kauto.engine.InferredType.Scalar
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.doubleOrNull
import kotlinx.serialization.json.longOrNull

/** Turns a JSON sample into an [InferredType] tree. */
object SchemaInferrer {

    private val json = Json { isLenient = true }

    class NonObjectRootException(message: String) : IllegalArgumentException(message)

    /**
     * Parses [text] and infers its schema. The root must be a JSON object, or
     * an array of objects (in which case the merged element shape is returned).
     */
    fun inferRoot(text: String, sourceName: String): ObjectType {
        val element = try {
            json.parseToJsonElement(text)
        } catch (e: Exception) {
            throw IllegalArgumentException("$sourceName is not valid JSON: ${e.message}", e)
        }
        return when (val inferred = infer(element)) {
            is ObjectType -> inferred
            is ArrayType -> inferred.element as? ObjectType
                ?: throw NonObjectRootException(
                    "$sourceName: root array elements must be JSON objects to generate a class",
                )
            else -> throw NonObjectRootException(
                "$sourceName: root must be a JSON object (or an array of objects) to generate a class",
            )
        }
    }

    fun infer(element: JsonElement): InferredType = when (element) {
        is JsonNull -> NullType()
        is JsonObject -> ObjectType(
            element.entries.associateTo(LinkedHashMap()) { (key, value) ->
                key to Field(infer(value))
            },
        )
        is JsonArray ->
            if (element.isEmpty()) {
                ArrayType(JsonAny())
            } else {
                ArrayType(element.map(::infer).reduce(TypeMerger::merge))
            }
        is JsonPrimitive -> inferPrimitive(element)
    }

    private fun inferPrimitive(primitive: JsonPrimitive): InferredType {
        if (primitive.isString) return Scalar(ScalarKind.STRING)
        primitive.booleanOrNull?.let { return Scalar(ScalarKind.BOOLEAN) }
        primitive.longOrNull?.let { value ->
            val kind = if (value in Int.MIN_VALUE..Int.MAX_VALUE) ScalarKind.INT else ScalarKind.LONG
            return Scalar(kind)
        }
        primitive.doubleOrNull?.let { return Scalar(ScalarKind.DOUBLE) }
        // Lenient mode can produce unquoted non-numeric literals; treat as text.
        return Scalar(ScalarKind.STRING)
    }
}
