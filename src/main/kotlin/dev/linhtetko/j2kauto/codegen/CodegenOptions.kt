package dev.linhtetko.j2kauto.codegen

import dev.linhtetko.j2kauto.Visibility

data class CodegenOptions(
    /** Generate `var` properties instead of `val`. */
    val useVar: Boolean = false,
    /** Give nullable properties a `= null` default so partial payloads decode ergonomically. */
    val defaultsForNullable: Boolean = true,
    /** Emit the name-mapping annotation on every property, not just renamed ones. */
    val alwaysAnnotate: Boolean = false,
    /** The visibility of the generated classes. */
    val visibility: Visibility = Visibility.PUBLIC,
)
