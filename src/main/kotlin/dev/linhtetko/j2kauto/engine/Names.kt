package dev.linhtetko.j2kauto.engine

/** Naming and sanitizing rules for generated properties and classes. */
object Names {

    private val WORD_DELIMITERS = Regex("[_\\-.\\s]+")
    private val INVALID_CHARS = Regex("[^A-Za-z0-9_\\-.\\s]")

    /**
     * `user_name` / `user-name` / `user name` → `userName`; already-camelCase
     * keys pass through. Invalid characters are stripped; a leading digit gets
     * a `_` prefix. Returns an empty string when nothing survives sanitizing —
     * callers substitute a positional fallback like `field1`.
     */
    fun propertyName(key: String): String {
        val words = words(key)
        if (words.isEmpty()) return ""
        val name = words.first().replaceFirstChar { it.lowercaseChar() } +
            words.drop(1).joinToString("") { it.replaceFirstChar { c -> c.uppercaseChar() } }
        return if (name.first().isDigit()) "_$name" else name
    }

    /** PascalCase class name from a key or file-name hint. Empty when nothing survives. */
    fun className(hint: String): String {
        val words = words(hint)
        if (words.isEmpty()) return ""
        val name = words.joinToString("") { it.replaceFirstChar { c -> c.uppercaseChar() } }
        return if (name.first().isDigit()) "_$name" else name
    }

    /**
     * Converts a relative directory path (e.g. "auth/v1") into a sanitized
     * Kotlin subpackage (e.g. ".auth.v1"). Returns an empty string if the
     * path is empty or only contains word separators.
     */
    fun subpackageName(relativePath: String): String {
        val segments = relativePath.split('/', '\\')
            .filter { it.isNotEmpty() }
            .map { segment ->
                val words = words(segment)
                if (words.isEmpty()) return@map ""
                val name = words.joinToString("") { it.lowercase() }
                if (name.first().isDigit()) "_$name" else name
            }
            .filter { it.isNotEmpty() }

        return if (segments.isEmpty()) "" else segments.joinToString(".", prefix = ".")
    }

    /**
     * Naive singularization for array-derived class names: `Items` → `Item`.
     * Heuristic only — leaves short words and `-ss` endings alone.
     */
    fun singularize(name: String): String =
        if (name.length > 3 && name.endsWith("s") && !name.endsWith("ss")) name.dropLast(1) else name

    private fun words(raw: String): List<String> =
        raw.replace(INVALID_CHARS, " ") // invalid chars act as word separators
            .split(WORD_DELIMITERS)
            .filter { it.isNotEmpty() }
}
