package dev.linhtetko.j2kauto.codegen

import com.squareup.kotlinpoet.AnnotationSpec
import com.squareup.kotlinpoet.TypeName
import dev.linhtetko.j2kauto.AnnotationStyle

/**
 * How one [AnnotationStyle] decorates the generated classes. Annotation class
 * names are built by string — the plugin never compiles against Moshi/Gson/
 * kotlinx.serialization annotation artifacts.
 */
interface AnnotationStrategy {

    fun classAnnotations(): List<AnnotationSpec>

    fun propertyAnnotations(originalKey: String, propertyName: String, always: Boolean): List<AnnotationSpec>

    /**
     * The fallback type for unknown/conflicting shapes. Must match what the
     * serializer can actually decode: `JsonElement` for kotlinx, `Any` otherwise.
     */
    fun anyType(): TypeName

    companion object {
        fun of(style: AnnotationStyle): AnnotationStrategy = when (style) {
            AnnotationStyle.KOTLINX -> KotlinxStrategy
            AnnotationStyle.MOSHI -> MoshiStrategy
            AnnotationStyle.GSON -> GsonStrategy
            AnnotationStyle.NONE -> NoneStrategy
        }
    }
}
