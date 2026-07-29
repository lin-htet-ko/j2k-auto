package dev.linhtetko.j2kauto.engine

import dev.linhtetko.j2kauto.engine.InferredType.ArrayType
import dev.linhtetko.j2kauto.engine.InferredType.Field
import dev.linhtetko.j2kauto.engine.InferredType.JsonAny
import dev.linhtetko.j2kauto.engine.InferredType.NullType
import dev.linhtetko.j2kauto.engine.InferredType.ObjectType
import dev.linhtetko.j2kauto.engine.InferredType.Scalar

/**
 * Unifies *compatible* object shapes — same key set, compatible field types —
 * into one canonical shape, within and across files, so the same JSON payload
 * described in several places generates a single class.
 *
 * Compatibility is deliberately stricter than [TypeMerger]: nullability
 * differences and numeric widening (Int→Long→Double) unify, but a merge that
 * would degrade a typed field to `JsonAny` means the shapes are different
 * things and stay separate.
 */
object ShapeUnifier {

    class Unification internal constructor(
        private val canonicalByShape: Map<ObjectType, ObjectType>,
    ) {
        /** Canonical shape for any object reachable from the unified roots. */
        fun canonicalOf(obj: ObjectType): ObjectType =
            canonicalByShape.getValue(SchemaRegistry.structuralKey(obj))
    }

    fun unify(roots: List<ObjectType>): Unification {
        // Collect every distinct object shape, then process by nesting height so
        // child clusters are final before any parent compares its fields.
        val heights = LinkedHashMap<ObjectType, Int>()
        roots.forEach { collect(it, heights) }
        val byHeight = heights.entries.sortedBy { it.value }

        val clusters = mutableListOf<Cluster>()
        val clusterByShape = HashMap<ObjectType, Cluster>()

        for ((shape, _) in byHeight) {
            // Rewrite object-typed fields to their (final) child canonicals first.
            val rewritten = rewrite(shape, clusterByShape)
            val existing = clusters.firstOrNull { cluster ->
                cluster.keySet == rewritten.fields.keys &&
                    compatibleMerge(cluster.canonical, rewritten, clusterByShape) != null
            }
            if (existing != null) {
                existing.canonical = compatibleMerge(existing.canonical, rewritten, clusterByShape)!!
                clusterByShape[shape] = existing
            } else {
                val cluster = Cluster(rewritten)
                clusters += cluster
                clusterByShape[shape] = cluster
            }
        }

        // Final canonicals may embed child canonicals that grew after the parent
        // joined its cluster (same-height siblings) — resolve once more, bottom-up.
        clusters.forEach { it.canonical = rewrite(it.canonical, clusterByShape) }

        return Unification(
            clusterByShape.entries.associate { (shape, cluster) ->
                SchemaRegistry.structuralKey(shape) to SchemaRegistry.structuralKey(cluster.canonical)
            },
        )
    }

    private class Cluster(var canonical: ObjectType) {
        val keySet: Set<String> get() = canonical.fields.keys
    }

    private fun collect(type: InferredType, heights: LinkedHashMap<ObjectType, Int>): Int = when (type) {
        is ObjectType -> {
            val key = SchemaRegistry.structuralKey(type)
            heights[key] ?: run {
                val childMax = type.fields.values.maxOfOrNull { collect(it.type, heights) } ?: -1
                val height = childMax + 1
                // re-put to keep first-seen order but final height
                heights[key] = maxOf(heights[key] ?: 0, height)
                height
            }
        }
        is ArrayType -> collect(type.element, heights)
        else -> -1
    }

    /** Replaces object-typed fields (incl. through arrays) with their cluster canonicals. */
    private fun rewrite(shape: ObjectType, clusterByShape: Map<ObjectType, Cluster>): ObjectType {
        val fields = shape.fields.entries.associateTo(LinkedHashMap()) { (key, field) ->
            key to Field(rewriteType(field.type, clusterByShape))
        }
        return ObjectType(fields, nullable = false)
    }

    private fun rewriteType(type: InferredType, clusterByShape: Map<ObjectType, Cluster>): InferredType =
        when (type) {
            is ObjectType -> {
                val canonical = clusterByShape[SchemaRegistry.structuralKey(type)]?.canonical
                canonical?.copy(nullable = type.nullable) ?: type
            }
            is ArrayType -> type.copy(element = rewriteType(type.element, clusterByShape))
            else -> type
        }

    /** Merged shape when [a] and [b] are compatible; null when they must stay separate. */
    internal fun compatibleMerge(a: ObjectType, b: ObjectType): ObjectType? =
        compatibleMerge(a, b, emptyMap())

    private fun compatibleMerge(
        a: ObjectType,
        b: ObjectType,
        clusterByShape: Map<ObjectType, Cluster>,
    ): ObjectType? {
        if (a.fields.keys != b.fields.keys) return null
        val fields = LinkedHashMap<String, Field>()
        for ((key, fieldA) in a.fields) {
            val merged = mergeCompatible(fieldA.type, b.fields.getValue(key).type, clusterByShape)
                ?: return null
            fields[key] = Field(merged)
        }
        return ObjectType(fields, nullable = a.nullable || b.nullable)
    }

    private fun mergeCompatible(
        x: InferredType,
        y: InferredType,
        clusterByShape: Map<ObjectType, Cluster>,
    ): InferredType? {
        val nullable = x.nullable || y.nullable
        return when {
            x is NullType -> y.asNullable()
            y is NullType -> x.asNullable()
            x is JsonAny && y is JsonAny -> JsonAny(nullable)
            x is JsonAny || y is JsonAny -> null // never degrade a typed field
            x is Scalar && y is Scalar -> mergeScalars(x.kind, y.kind, nullable)
            x is ArrayType && y is ArrayType ->
                mergeCompatible(x.element, y.element, clusterByShape)?.let { ArrayType(it, nullable) }
            x is ObjectType && y is ObjectType -> mergeObjects(x, y, nullable, clusterByShape)
            else -> null
        }
    }

    private fun mergeScalars(a: ScalarKind, b: ScalarKind, nullable: Boolean): InferredType? {
        if (a == b) return Scalar(a, nullable)
        val numeric = setOf(ScalarKind.INT, ScalarKind.LONG, ScalarKind.DOUBLE)
        if (a in numeric && b in numeric) {
            val widened = if (ScalarKind.DOUBLE in listOf(a, b)) ScalarKind.DOUBLE else ScalarKind.LONG
            return Scalar(widened, nullable)
        }
        return null
    }

    private fun mergeObjects(
        x: ObjectType,
        y: ObjectType,
        nullable: Boolean,
        clusterByShape: Map<ObjectType, Cluster>,
    ): InferredType? {
        val clusterX = clusterByShape[SchemaRegistry.structuralKey(x)]
        val clusterY = clusterByShape[SchemaRegistry.structuralKey(y)]
        return when {
            // Both already clustered (child height): unify only if same cluster.
            clusterX != null || clusterY != null ->
                if (clusterX === clusterY) clusterX!!.canonical.copy(nullable = nullable) else null
            // Unclustered (direct compatibleMerge call in tests): structural recursion.
            else -> compatibleMerge(x, y)?.copy(nullable = nullable)
        }
    }
}
