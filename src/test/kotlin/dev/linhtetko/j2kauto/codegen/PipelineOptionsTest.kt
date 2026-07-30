package dev.linhtetko.j2kauto.codegen

import dev.linhtetko.j2kauto.AnnotationStyle
import dev.linhtetko.j2kauto.Visibility
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class PipelineOptionsTest {

    private val input = JsonInput("pet.json", """{"pet_name": "Milo", "note": null}""")

    private fun generate(options: CodegenOptions): String =
        J2kPipeline.generate(listOf(input), "com.example", AnnotationStyle.KOTLINX, options)
            .single()
            .toString()

    @Test
    fun `useVar emits mutable properties`() {
        assertTrue("public var petName: String" in generate(CodegenOptions(useVar = true)))
    }

    @Test
    fun `defaultsForNullable off drops null defaults`() {
        val text = generate(CodegenOptions(defaultsForNullable = false))
        assertTrue("public val note: JsonElement?," in text)
        assertFalse("= null" in text)
    }

    @Test
    fun `alwaysAnnotate stamps every property`() {
        val text = generate(CodegenOptions(alwaysAnnotate = true))
        assertTrue("@SerialName(\"pet_name\")" in text)
        assertTrue("@SerialName(\"note\")" in text)
    }

    @Test
    fun `internal visibility applies internal modifier`() {
        val text = generate(CodegenOptions(visibility = Visibility.INTERNAL))
        assertTrue("internal data class Pet(" in text)
    }

    @Test
    fun `private visibility applies private modifier`() {
        val text = generate(CodegenOptions(visibility = Visibility.PRIVATE))
        assertTrue("private data class Pet(" in text)
    }

    @Test
    fun `public visibility applies public modifier explicitly`() {
        val text = generate(CodegenOptions(visibility = Visibility.PUBLIC))
        assertTrue("public data class Pet(" in text)
    }

    @Test
    fun `rootClassName override wins over file name`() {
        val spec = J2kPipeline.generate(
            listOf(input),
            "com.example",
            AnnotationStyle.KOTLINX,
            rootClassNames = mapOf("pet.json" to "Companion2"),
        ).single()
        assertTrue("public data class Companion2(" in spec.toString())
    }

    @Test
    fun `cross-file nested class collisions are prefixed with the file root name`() {
        val a = JsonInput("order_a.json", """{"user": {"id": 1}}""")
        val b = JsonInput("order_b.json", """{"user": {"name": "x"}}""")

        val specs = J2kPipeline.generate(listOf(a, b), "com.example", AnnotationStyle.NONE)
        val texts = specs.map { it.toString() }

        assertTrue(texts[0].contains("data class User(") )
        assertTrue(texts[1].contains("data class OrderBUser("))
        assertTrue(texts[1].contains("val user: OrderBUser"))
        assertEquals(2, specs.size)
    }

    @Test
    fun `identical files unify into a single generated file`() {
        val a = JsonInput("a.json", """{"meta": {"v": 1}}""")
        val b = JsonInput("b.json", """{"meta": {"v": 1}}""")

        val specs = J2kPipeline.generate(listOf(a, b), "com.example", AnnotationStyle.NONE)

        assertEquals(1, specs.size)
        val text = specs.single().toString()
        assertTrue("data class A(" in text)
        assertTrue("data class Meta(" in text)
    }

    @Test
    fun `compatible shapes across files unify into one shared class - product case`() {
        val product = JsonInput(
            "product.json",
            """{"product_id": 1001, "name": "Kibble", "rating": {"average": 4.6, "count": 120}}""",
        )
        val catalog = JsonInput(
            "product_catalog.json",
            """
            {
              "catalog_id": "dog-food",
              "products": [
                {"product_id": 1001, "name": "Kibble", "rating": {"average": 4.6, "count": 120}},
                {"product_id": 1002, "name": "Starter"}
              ]
            }
            """.trimIndent(),
        )

        val specs = J2kPipeline.generate(listOf(product, catalog), "com.example", AnnotationStyle.NONE)
        val productText = specs[0].toString()
        val catalogText = specs[1].toString()

        // One shared Product class (in product.json's file), rating nullable via unification.
        assertTrue("data class Product(" in productText)
        assertTrue("val rating: Rating? = null" in productText)
        assertTrue("val products: List<Product>" in catalogText)
        assertFalse(catalogText.contains("ProductCatalogProduct"))
        assertFalse(catalogText.contains("data class Product("))
    }
}
