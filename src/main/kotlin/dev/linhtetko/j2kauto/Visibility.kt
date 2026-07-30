package dev.linhtetko.j2kauto

/**
 * Visibility modifiers for generated Kotlin classes.
 */
enum class Visibility {
    /** The class is visible everywhere. */
    PUBLIC,

    /** The class is visible only within the same Gradle module. */
    INTERNAL,

    /** The class is visible only within the file it's generated in. */
    PRIVATE,
}
