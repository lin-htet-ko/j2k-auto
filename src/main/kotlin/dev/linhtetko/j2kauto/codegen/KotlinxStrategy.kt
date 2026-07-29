package dev.linhtetko.j2kauto.codegen

import com.squareup.kotlinpoet.AnnotationSpec
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.TypeName

internal object KotlinxStrategy : AnnotationStrategy {

    private val serializable = ClassName("kotlinx.serialization", "Serializable")
    private val serialName = ClassName("kotlinx.serialization", "SerialName")
    private val jsonElement = ClassName("kotlinx.serialization.json", "JsonElement")

    override fun classAnnotations(): List<AnnotationSpec> =
        listOf(AnnotationSpec.builder(serializable).build())

    override fun propertyAnnotations(
        originalKey: String,
        propertyName: String,
        always: Boolean,
    ): List<AnnotationSpec> =
        if (always || originalKey != propertyName) {
            listOf(AnnotationSpec.builder(serialName).addMember("%S", originalKey).build())
        } else {
            emptyList()
        }

    override fun anyType(): TypeName = jsonElement
}
