package dev.linhtetko.j2kauto.engine

import dev.linhtetko.j2kauto.engine.InferredType.ArrayType
import dev.linhtetko.j2kauto.engine.InferredType.Field
import dev.linhtetko.j2kauto.engine.InferredType.ObjectType
import dev.linhtetko.j2kauto.engine.InferredType.Scalar
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class SchemaRegistryTest {

    private fun obj(vararg fields: Pair<String, InferredType>) =
        ObjectType(fields.associateTo(LinkedHashMap()) { it.first to Field(it.second) })

    @Test
    fun `identical shapes dedupe into one class`() {
        val address = obj("street" to Scalar(ScalarKind.STRING))
        val root = obj(
            "home_address" to address,
            "work_address" to address.copy(),
        )

        val classes = SchemaRegistry().register(root, "User")

        assertEquals(listOf("User", "HomeAddress"), classes.map { it.name })
    }

    @Test
    fun `different shapes with colliding hints get parent prefix`() {
        val root = obj(
            "owner" to obj("name" to Scalar(ScalarKind.STRING)),
            "pet" to obj(
                "owner" to obj("id" to Scalar(ScalarKind.INT)),
            ),
        )

        val classes = SchemaRegistry().register(root, "Root")

        assertEquals(listOf("Root", "Owner", "Pet", "PetOwner"), classes.map { it.name })
    }

    @Test
    fun `array-derived class names are singularized`() {
        val root = obj(
            "items" to ArrayType(obj("sku" to Scalar(ScalarKind.STRING))),
        )

        val classes = SchemaRegistry().register(root, "Order")

        assertEquals(listOf("Order", "Item"), classes.map { it.name })
    }

    @Test
    fun `shared registry reuses classes across files - identical shape registers once`() {
        val registry = SchemaRegistry()
        val meta = obj("v" to Scalar(ScalarKind.INT))

        val deltaA = registry.register(obj("meta" to meta), "A")
        val deltaB = registry.register(obj("meta" to meta.copy()), "B")

        assertEquals(listOf("A", "Meta"), deltaA.map { it.name })
        // B's root is a distinct shape? No — same fields as A's root → dedupes entirely.
        assertEquals(emptyList(), deltaB.map { it.name })
        assertEquals(listOf("A", "Meta"), registry.allClasses.map { it.name })
    }

    @Test
    fun `shared registry parent-prefixes cross-file name collisions of different shapes`() {
        val registry = SchemaRegistry()

        val deltaA = registry.register(obj("user" to obj("id" to Scalar(ScalarKind.INT))), "OrderA")
        val deltaB = registry.register(obj("user" to obj("name" to Scalar(ScalarKind.STRING))), "OrderB")

        assertEquals(listOf("OrderA", "User"), deltaA.map { it.name })
        assertEquals(listOf("OrderB", "OrderBUser"), deltaB.map { it.name })
    }

    @Test
    fun `empty nested objects are not registered as classes`() {
        val registry = SchemaRegistry()

        val delta = registry.register(obj("meta" to obj()), "Root")

        assertEquals(listOf("Root"), delta.map { it.name })
    }
}
