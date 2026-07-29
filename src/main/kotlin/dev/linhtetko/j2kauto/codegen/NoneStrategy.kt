package dev.linhtetko.j2kauto.codegen

import com.squareup.kotlinpoet.ANY
import com.squareup.kotlinpoet.AnnotationSpec
import com.squareup.kotlinpoet.TypeName

internal object NoneStrategy : AnnotationStrategy {

    override fun classAnnotations(): List<AnnotationSpec> = emptyList()

    override fun propertyAnnotations(
        originalKey: String,
        propertyName: String,
        always: Boolean,
    ): List<AnnotationSpec> = emptyList()

    override fun anyType(): TypeName = ANY
}
