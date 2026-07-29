package dev.linhtetko.j2kauto.engine

/**
 * The schema inferred from JSON samples. Nullability is a flag on the node
 * rather than a wrapper type so that merge rules stay single-dispatch and
 * structural equality (used for shape dedupe) stays trivial.
 */
sealed interface InferredType {
    val nullable: Boolean

    fun asNullable(): InferredType

    data class Scalar(
        val kind: ScalarKind,
        override val nullable: Boolean = false,
    ) : InferredType {
        override fun asNullable() = copy(nullable = true)
    }

    data class ArrayType(
        val element: InferredType,
        override val nullable: Boolean = false,
    ) : InferredType {
        override fun asNullable() = copy(nullable = true)
    }

    data class ObjectType(
        val fields: LinkedHashMap<String, Field>,
        override val nullable: Boolean = false,
    ) : InferredType {
        override fun asNullable() = copy(nullable = true)
    }

    /** Unknown / conflicting shape — maps to the strategy's "any" type at codegen. */
    data class JsonAny(
        override val nullable: Boolean = true,
    ) : InferredType {
        override fun asNullable() = copy(nullable = true)
    }

    /**
     * A JSON `null` seen on its own. Lattice bottom: merged with a concrete
     * type it disappears into `concrete.asNullable()`; never merged, it
     * resolves to a nullable [JsonAny] at codegen time.
     */
    data class NullType(
        override val nullable: Boolean = true,
    ) : InferredType {
        override fun asNullable() = this
    }

    /** One field of an [ObjectType], keyed by the ORIGINAL json key. */
    data class Field(val type: InferredType)
}

enum class ScalarKind { BOOLEAN, INT, LONG, DOUBLE, STRING }
