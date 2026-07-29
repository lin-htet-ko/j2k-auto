package dev.linhtetko.j2kauto.codegen

import com.squareup.kotlinpoet.ANY
import com.squareup.kotlinpoet.AnnotationSpec
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.TypeName

internal object MoshiStrategy : AnnotationStrategy {

    private val jsonClass = ClassName("com.squareup.moshi", "JsonClass")
    private val json = ClassName("com.squareup.moshi", "Json")

    override fun classAnnotations(): List<AnnotationSpec> =
        listOf(AnnotationSpec.builder(jsonClass).addMember("generateAdapter = true").build())

    override fun propertyAnnotations(
        originalKey: String,
        propertyName: String,
        always: Boolean,
    ): List<AnnotationSpec> =
        if (always || originalKey != propertyName) {
            listOf(AnnotationSpec.builder(json).addMember("name = %S", originalKey).build())
        } else {
            emptyList()
        }

    override fun anyType(): TypeName = ANY
}
