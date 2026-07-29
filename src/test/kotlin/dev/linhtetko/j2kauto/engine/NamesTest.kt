package dev.linhtetko.j2kauto.engine

import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class NamesTest {

    @Test
    fun `snake kebab space and dot keys become camelCase`() {
        assertEquals("userName", Names.propertyName("user_name"))
        assertEquals("userName", Names.propertyName("user-name"))
        assertEquals("userName", Names.propertyName("user name"))
        assertEquals("userName", Names.propertyName("user.name"))
    }

    @Test
    fun `camelCase keys pass through`() {
        assertEquals("userName", Names.propertyName("userName"))
        assertEquals("id", Names.propertyName("id"))
    }

    @Test
    fun `invalid characters are stripped`() {
        assertEquals("price", Names.propertyName("price$"))
        assertEquals("aB", Names.propertyName("a@b"))
    }

    @Test
    fun `emoji-only key sanitizes to empty for caller fallback`() {
        assertEquals("", Names.propertyName("🐶🐱"))
    }

    @Test
    fun `leading digit gets underscore prefix`() {
        assertEquals("_1stPlace", Names.propertyName("1st_place"))
        assertEquals("_2ndItem", Names.className("2nd_item"))
    }

    @Test
    fun `class names are PascalCase`() {
        assertEquals("UserProfile", Names.className("user_profile"))
        assertEquals("UserProfile", Names.className("user-profile"))
    }

    @Test
    fun `singularize strips trailing s conservatively`() {
        assertEquals("Item", Names.singularize("Items"))
        assertEquals("Address", Names.singularize("Address"))
        assertEquals("Ids", Names.singularize("Ids"))
    }

    @Test
    fun `hard keywords survive as-is - codegen escapes them with backticks`() {
        assertEquals("object", Names.propertyName("object"))
        assertEquals("when", Names.propertyName("when"))
    }
}
