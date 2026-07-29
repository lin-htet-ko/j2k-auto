package dev.linhtetko.j2kauto.engine

import dev.linhtetko.j2kauto.engine.InferredType.ArrayType
import dev.linhtetko.j2kauto.engine.InferredType.Field
import dev.linhtetko.j2kauto.engine.InferredType.JsonAny
import dev.linhtetko.j2kauto.engine.InferredType.ObjectType
import dev.linhtetko.j2kauto.engine.InferredType.Scalar
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertNull

class ShapeUnifierTest {

    private fun obj(vararg fields: Pair<String, InferredType>) =
        ObjectType(fields.associateTo(LinkedHashMap()) { it.first to Field(it.second) })

    @Test
    fun `shapes differing only in field nullability unify - the product case`() {
        val rating = obj("average" to Scalar(ScalarKind.DOUBLE), "count" to Scalar(ScalarKind.INT))
        val productExact = obj("product_id" to Scalar(ScalarKind.INT), "rating" to rating)
        val productMerged = obj(
            "product_id" to Scalar(ScalarKind.INT),
            "rating" to rating.copy(nullable = true),
        )
        val rootA = obj("p" to productExact)
        val rootB = obj("items" to ArrayType(productMerged))

        val unification = ShapeUnifier.unify(listOf(rootA, rootB))

        val canonical = unification.canonicalOf(productExact)
        assertEquals(canonical, unification.canonicalOf(productMerged))
        // nullability ORs through: the unified field is nullable
        assertEquals(true, canonical.fields.getValue("rating").type.nullable)
    }

    @Test
    fun `numeric widening unifies int and double fields`() {
        val a = obj("price" to Scalar(ScalarKind.INT))
        val b = obj("price" to Scalar(ScalarKind.DOUBLE))

        val unification = ShapeUnifier.unify(listOf(obj("x" to a), obj("y" to b)))

        assertEquals(unification.canonicalOf(a), unification.canonicalOf(b))
        assertEquals(
            Scalar(ScalarKind.DOUBLE),
            unification.canonicalOf(a).fields.getValue("price").type,
        )
    }

    @Test
    fun `different key sets stay separate`() {
        val a = obj("id" to Scalar(ScalarKind.INT))
        val b = obj("id" to Scalar(ScalarKind.INT), "name" to Scalar(ScalarKind.STRING))

        val unification = ShapeUnifier.unify(listOf(obj("x" to a), obj("y" to b)))

        assertNotEquals(unification.canonicalOf(a), unification.canonicalOf(b))
    }

    @Test
    fun `conflicting field types stay separate - no JsonAny degradation`() {
        val a = obj("id" to Scalar(ScalarKind.INT))
        val b = obj("id" to Scalar(ScalarKind.STRING))

        val unification = ShapeUnifier.unify(listOf(obj("x" to a), obj("y" to b)))

        assertNotEquals(unification.canonicalOf(a), unification.canonicalOf(b))
        assertNull(ShapeUnifier.compatibleMerge(a, b))
    }

    @Test
    fun `typed array does not unify with unknown-element array`() {
        val a = obj("tags" to ArrayType(Scalar(ScalarKind.STRING)))
        val b = obj("tags" to ArrayType(JsonAny()))

        val unification = ShapeUnifier.unify(listOf(obj("x" to a), obj("y" to b)))

        assertNotEquals(unification.canonicalOf(a), unification.canonicalOf(b))
    }

    @Test
    fun `parents unify when their nested objects unified`() {
        val childA = obj("v" to Scalar(ScalarKind.INT))
        val childB = obj("v" to Scalar(ScalarKind.LONG))
        val parentA = obj("child" to childA)
        val parentB = obj("child" to childB)

        val unification = ShapeUnifier.unify(listOf(obj("a" to parentA), obj("b" to parentB)))

        assertEquals(unification.canonicalOf(parentA), unification.canonicalOf(parentB))
        assertEquals(
            Scalar(ScalarKind.LONG),
            unification.canonicalOf(childA).fields.getValue("v").type,
        )
    }
}
