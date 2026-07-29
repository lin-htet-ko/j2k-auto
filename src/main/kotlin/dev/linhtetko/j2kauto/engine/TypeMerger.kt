package dev.linhtetko.j2kauto.engine

import dev.linhtetko.j2kauto.engine.InferredType.ArrayType
import dev.linhtetko.j2kauto.engine.InferredType.Field
import dev.linhtetko.j2kauto.engine.InferredType.JsonAny
import dev.linhtetko.j2kauto.engine.InferredType.NullType
import dev.linhtetko.j2kauto.engine.InferredType.ObjectType
import dev.linhtetko.j2kauto.engine.InferredType.Scalar

/**
 * Merges two inferred types into the narrowest type that can represent both.
 * Commutative and associative; nullability flags OR through every rule.
 */
object TypeMerger {

    fun merge(a: InferredType, b: InferredType): InferredType {
        val nullable = a.nullable || b.nullable
        return when {
            a is NullType -> b.asNullable()
            b is NullType -> a.asNullable()
            a is JsonAny || b is JsonAny -> JsonAny(nullable)
            a is Scalar && b is Scalar -> mergeScalars(a.kind, b.kind, nullable)
            a is ArrayType && b is ArrayType -> ArrayType(merge(a.element, b.element), nullable)
            a is ObjectType && b is ObjectType -> mergeObjects(a, b, nullable)
            else -> JsonAny(nullable)
        }
    }

    private fun mergeScalars(a: ScalarKind, b: ScalarKind, nullable: Boolean): InferredType {
        if (a == b) return Scalar(a, nullable)
        val numeric = setOf(ScalarKind.INT, ScalarKind.LONG, ScalarKind.DOUBLE)
        if (a in numeric && b in numeric) {
            val widened = if (ScalarKind.DOUBLE in listOf(a, b)) ScalarKind.DOUBLE else ScalarKind.LONG
            return Scalar(widened, nullable)
        }
        return JsonAny(nullable)
    }

    private fun mergeObjects(a: ObjectType, b: ObjectType, nullable: Boolean): ObjectType {
        val fields = LinkedHashMap<String, Field>()
        for ((key, field) in a.fields) {
            val other = b.fields[key]
            fields[key] = if (other == null) {
                Field(field.type.asNullable())
            } else {
                Field(merge(field.type, other.type))
            }
        }
        for ((key, field) in b.fields) {
            if (key !in fields) fields[key] = Field(field.type.asNullable())
        }
        return ObjectType(fields, nullable)
    }
}
