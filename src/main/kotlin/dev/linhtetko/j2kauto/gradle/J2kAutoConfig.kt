package dev.linhtetko.j2kauto.gradle

import dev.linhtetko.j2kauto.AnnotationStyle
import dev.linhtetko.j2kauto.Visibility
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.provider.MapProperty
import org.gradle.api.provider.Property

interface J2kAutoConfig {
    val packageName: Property<String>
    val annotationStyle: Property<AnnotationStyle>
    val visibility: Property<Visibility>
    val useVar: Property<Boolean>
    val defaultsForNullable: Property<Boolean>
    val alwaysAnnotate: Property<Boolean>
    val sources: ConfigurableFileCollection
    val rootClassNames: MapProperty<String, String>

    fun source(dir: Any) {
        sources.from(dir)
    }

    fun rootClassName(fileName: String, className: String) {
        rootClassNames.put(fileName, className)
    }
}
