package dev.linhtetko.j2kauto.gradle

import dev.linhtetko.j2kauto.AnnotationStyle
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
abstract class J2kAutoExtension @Inject constructor(objects: ObjectFactory) {

    /** Package of the generated classes. Default: `generated.j2kauto`. */
    abstract val packageName: Property<String>

    /** Which JSON-mapping annotations to emit. Default: [AnnotationStyle.KOTLINX]. */
    abstract val annotationStyle: Property<AnnotationStyle>

    /** Generate `var` properties instead of `val`. Default: false. */
    abstract val useVar: Property<Boolean>

    /** Give nullable properties a `= null` default. Default: true. */
    abstract val defaultsForNullable: Property<Boolean>

    /** Emit the name-mapping annotation on every property, not just renamed ones. Default: false. */
    abstract val alwaysAnnotate: Property<Boolean>

    internal val sources: ConfigurableFileCollection = objects.fileCollection()

    internal abstract val rootClassNames: MapProperty<String, String>

    /** Adds a directory (or file) of `.json` samples. Defaults to `src/main/json` when never called. */
    fun source(dir: Any) {
        sources.from(dir)
    }

    /** Overrides the root class name for one input file (default: PascalCased file name). */
    fun rootClassName(fileName: String, className: String) {
        rootClassNames.put(fileName, className)
    }
}
