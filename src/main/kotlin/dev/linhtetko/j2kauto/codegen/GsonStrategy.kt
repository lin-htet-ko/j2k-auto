package dev.linhtetko.j2kauto.codegen

import com.squareup.kotlinpoet.ANY
import com.squareup.kotlinpoet.AnnotationSpec
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.TypeName

internal object GsonStrategy : AnnotationStrategy {

    private val serializedName = ClassName("com.google.gson.annotations", "SerializedName")

    override fun classAnnotations(): List<AnnotationSpec> = emptyList()

    override fun propertyAnnotations(
        originalKey: String,
        propertyName: String,
        always: Boolean,
    ): List<AnnotationSpec> =
        if (always || originalKey != propertyName) {
            listOf(AnnotationSpec.builder(serializedName).addMember("%S", originalKey).build())
        } else {
            emptyList()
        }

    override fun anyType(): TypeName = ANY
}
