package dev.linhtetko.j2kauto.gradle

import dev.linhtetko.j2kauto.AnnotationStyle
import dev.linhtetko.j2kauto.Visibility
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.MapProperty
import org.gradle.api.provider.Property
import javax.inject.Inject

/**
 * ```
 * j2kAuto {
 *     packageName = "com.example.model"
 *     annotationStyle = AnnotationStyle.KOTLINX
 *     source(layout.projectDirectory.dir("src/main/json"))   // default when omitted
 *     rootClassName("user_profile.json", "Profile")
 * }
 * ```
 */
abstract class J2kAutoExtension @Inject constructor(objects: ObjectFactory) : J2kAutoConfig {

    /** Package of the generated classes. Default: `generated.j2kauto`. */
    abstract override val packageName: Property<String>

    /** Which JSON-mapping annotations to emit. Default: [AnnotationStyle.KOTLINX]. */
    abstract override val annotationStyle: Property<AnnotationStyle>

    /** Visibility of the generated classes. Default: [Visibility.PUBLIC]. */
    abstract override val visibility: Property<Visibility>

    /** Generate `var` properties instead of `val`. Default: false. */
    abstract override val useVar: Property<Boolean>

    /** Give nullable properties a `= null` default. Default: true. */
    abstract override val defaultsForNullable: Property<Boolean>

    /** Emit the name-mapping annotation on every property, not just renamed ones. Default: false. */
    abstract override val alwaysAnnotate: Property<Boolean>

    override val sources: ConfigurableFileCollection = objects.fileCollection()

    abstract override val rootClassNames: MapProperty<String, String>

    val targets = objects.domainObjectContainer(J2kAutoTarget::class.java)

    fun targets(action: org.gradle.api.Action<org.gradle.api.NamedDomainObjectContainer<J2kAutoTarget>>) {
        action.execute(targets)
    }
}
