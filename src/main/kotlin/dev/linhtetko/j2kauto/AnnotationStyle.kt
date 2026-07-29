package dev.linhtetko.j2kauto

/**
 * Which JSON-mapping annotations the generated data classes carry.
 */
enum class AnnotationStyle {
    /** kotlinx.serialization — `@Serializable` classes, `@SerialName` on renamed properties. */
    KOTLINX,

    /** Moshi — `@JsonClass(generateAdapter = true)` classes, `@Json(name = …)` on renamed properties. */
    MOSHI,

    /** Gson — `@SerializedName` on renamed properties. */
    GSON,

    /** Plain Kotlin data classes without any annotations. */
    NONE,
}
