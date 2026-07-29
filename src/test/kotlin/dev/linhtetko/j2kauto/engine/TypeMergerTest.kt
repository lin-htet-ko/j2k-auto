package dev.linhtetko.j2kauto.engine

import dev.linhtetko.j2kauto.engine.InferredType.ArrayType
import dev.linhtetko.j2kauto.engine.InferredType.Field
import dev.linhtetko.j2kauto.engine.InferredType.JsonAny
import dev.linhtetko.j2kauto.engine.InferredType.NullType
import dev.linhtetko.j2kauto.engine.InferredType.ObjectType
import dev.linhtetko.j2kauto.engine.InferredType.Scalar
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TypeMergerTest {

    private fun obj(vararg fields: Pair<String, InferredType>) =
        ObjectType(fields.associateTo(LinkedHashMap()) { it.first to Field(it.second) })

    @Test
    fun `null plus concrete becomes nullable concrete`() {
        assertEquals(
            Scalar(ScalarKind.INT, nullable = true),
            TypeMerger.merge(NullType(), Scalar(ScalarKind.INT)),
        )
        assertEquals(
            Scalar(ScalarKind.INT, nullable = true),
            TypeMerger.merge(Scalar(ScalarKind.INT), NullType()),
        )
    }

    @Test
    fun `int widens to long then double`() {
        assertEquals(
            Scalar(ScalarKind.LONG),
            TypeMerger.merge(Scalar(ScalarKind.INT), Scalar(ScalarKind.LONG)),
        )
        assertEquals(
            Scalar(ScalarKind.DOUBLE),
            TypeMerger.merge(Scalar(ScalarKind.INT), Scalar(ScalarKind.DOUBLE)),
        )
        assertEquals(
            Scalar(ScalarKind.DOUBLE),
            TypeMerger.merge(Scalar(ScalarKind.LONG), Scalar(ScalarKind.DOUBLE)),
        )
    }

    @Test
    fun `conflicting scalar kinds collapse to any`() {
        val merged = TypeMerger.merge(Scalar(ScalarKind.STRING), Scalar(ScalarKind.INT))
        assertTrue(merged is JsonAny)
    }

    @Test
    fun `same kind is preserved and nullability ors through`() {
        assertEquals(
            Scalar(ScalarKind.STRING, nullable = true),
            TypeMerger.merge(Scalar(ScalarKind.STRING, nullable = true), Scalar(ScalarKind.STRING)),
        )
    }

    @Test
    fun `arrays merge element types`() {
        assertEquals(
            ArrayType(Scalar(ScalarKind.DOUBLE)),
            TypeMerger.merge(ArrayType(Scalar(ScalarKind.INT)), ArrayType(Scalar(ScalarKind.DOUBLE))),
        )
    }

    @Test
    fun `object merge unions keys and marks one-sided fields nullable`() {
        val a = obj("id" to Scalar(ScalarKind.INT), "name" to Scalar(ScalarKind.STRING))
        val b = obj("id" to Scalar(ScalarKind.LONG), "email" to Scalar(ScalarKind.STRING))

        val merged = TypeMerger.merge(a, b) as ObjectType

        assertEquals(Scalar(ScalarKind.LONG), merged.fields.getValue("id").type)
        assertEquals(Scalar(ScalarKind.STRING, nullable = true), merged.fields.getValue("name").type)
        assertEquals(Scalar(ScalarKind.STRING, nullable = true), merged.fields.getValue("email").type)
        assertEquals(listOf("id", "name", "email"), merged.fields.keys.toList())
    }

    @Test
    fun `object plus non-object collapses to any`() {
        val merged = TypeMerger.merge(obj("id" to Scalar(ScalarKind.INT)), Scalar(ScalarKind.STRING))
        assertTrue(merged is JsonAny)
    }

    @Test
    fun `merge is commutative and associative for mixed shapes`() {
        val types = listOf(
            Scalar(ScalarKind.INT),
            Scalar(ScalarKind.DOUBLE),
            NullType(),
            ArrayType(Scalar(ScalarKind.STRING)),
            obj("x" to Scalar(ScalarKind.INT)),
        )
        for (a in types) for (b in types) {
            assertEquals(TypeMerger.merge(a, b), TypeMerger.merge(b, a), "commutativity of $a + $b")
            for (c in types) {
                assertEquals(
                    TypeMerger.merge(TypeMerger.merge(a, b), c),
                    TypeMerger.merge(a, TypeMerger.merge(b, c)),
                    "associativity of $a + $b + $c",
                )
            }
        }
    }
}
