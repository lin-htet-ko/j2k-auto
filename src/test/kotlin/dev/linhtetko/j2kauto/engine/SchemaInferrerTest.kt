package dev.linhtetko.j2kauto.engine

import dev.linhtetko.j2kauto.engine.InferredType.ArrayType
import dev.linhtetko.j2kauto.engine.InferredType.JsonAny
import dev.linhtetko.j2kauto.engine.InferredType.ObjectType
import dev.linhtetko.j2kauto.engine.InferredType.Scalar
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SchemaInferrerTest {

    private fun rootOf(json: String): ObjectType = SchemaInferrer.inferRoot(json, "test.json")

    @Test
    fun `scalar kinds are detected`() {
        val root = rootOf(
            """{"b": true, "i": 1, "l": ${Int.MAX_VALUE.toLong() + 1}, "d": 1.5, "s": "x"}""",
        )
        assertEquals(Scalar(ScalarKind.BOOLEAN), root.fields.getValue("b").type)
        assertEquals(Scalar(ScalarKind.INT), root.fields.getValue("i").type)
        assertEquals(Scalar(ScalarKind.LONG), root.fields.getValue("l").type)
        assertEquals(Scalar(ScalarKind.DOUBLE), root.fields.getValue("d").type)
        assertEquals(Scalar(ScalarKind.STRING), root.fields.getValue("s").type)
    }

    @Test
    fun `int boundary values stay int`() {
        val root = rootOf("""{"max": ${Int.MAX_VALUE}, "min": ${Int.MIN_VALUE}}""")
        assertEquals(Scalar(ScalarKind.INT), root.fields.getValue("max").type)
        assertEquals(Scalar(ScalarKind.INT), root.fields.getValue("min").type)
    }

    @Test
    fun `nested objects and arrays are inferred recursively`() {
        val root = rootOf("""{"user": {"tags": ["a", "b"]}}""")
        val user = root.fields.getValue("user").type as ObjectType
        assertEquals(ArrayType(Scalar(ScalarKind.STRING)), user.fields.getValue("tags").type)
    }

    @Test
    fun `array elements merge - missing field becomes nullable`() {
        val root = rootOf("""{"items": [{"id": 1, "note": "x"}, {"id": 2}]}""")
        val element = (root.fields.getValue("items").type as ArrayType).element as ObjectType
        assertEquals(Scalar(ScalarKind.INT), element.fields.getValue("id").type)
        assertEquals(Scalar(ScalarKind.STRING, nullable = true), element.fields.getValue("note").type)
    }

    @Test
    fun `null-only field resolves to null type`() {
        val root = rootOf("""{"x": null}""")
        assertTrue(root.fields.getValue("x").type is InferredType.NullType)
    }

    @Test
    fun `empty array falls back to any element`() {
        val root = rootOf("""{"xs": []}""")
        assertEquals(ArrayType(JsonAny()), root.fields.getValue("xs").type)
    }

    @Test
    fun `heterogeneous array collapses to any`() {
        val root = rootOf("""{"xs": [1, "two"]}""")
        val element = (root.fields.getValue("xs").type as ArrayType).element
        assertTrue(element is JsonAny)
    }

    @Test
    fun `root array of objects returns merged element shape`() {
        val root = rootOf("""[{"id": 1}, {"id": 2, "name": "x"}]""")
        assertEquals(Scalar(ScalarKind.INT), root.fields.getValue("id").type)
        assertEquals(Scalar(ScalarKind.STRING, nullable = true), root.fields.getValue("name").type)
    }

    @Test
    fun `root scalar is rejected with a clear error`() {
        assertThrows<SchemaInferrer.NonObjectRootException> { rootOf("42") }
        assertThrows<SchemaInferrer.NonObjectRootException> { rootOf("[1, 2]") }
    }

    @Test
    fun `invalid json is rejected with the source name in the message`() {
        val error = assertThrows<IllegalArgumentException> { rootOf("{unclosed") }
        assertTrue("test.json" in error.message.orEmpty())
    }
}
