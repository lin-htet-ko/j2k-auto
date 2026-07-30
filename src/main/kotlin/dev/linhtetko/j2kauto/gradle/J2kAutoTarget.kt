package dev.linhtetko.j2kauto.gradle

import org.gradle.api.model.ObjectFactory
import javax.inject.Inject

abstract class J2kAutoTarget @Inject constructor(
    val name: String,
    objects: ObjectFactory
) : J2kAutoConfig {
    override val sources = objects.fileCollection()
}
