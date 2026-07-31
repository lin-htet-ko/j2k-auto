# AI Context for j2k-auto

This page provides a high-density technical summary of `j2k-auto` for AI agents and LLMs to assist in development, integration, and debugging.

## Core Architecture

`j2k-auto` is composed of three primary layers:

1.  **Inference Engine**: Located in `dev.linhtetko.j2kauto.engine`. It performs lattice-based type merging to unify JSON shapes into consistent Kotlin models.
2.  **Code Generation**: Located in `dev.linhtetko.j2kauto.codegen`. Uses **KotlinPoet** to emit idiomatic Kotlin code.
3.  **Gradle Plugin**: Located in `dev.linhtetko.j2kauto.gradle`. Handles task wiring, DSL parsing, and build-time execution.

## Gradle DSL Reference

```kotlin
j2kAuto {
    // Base package for generated classes
    packageName = "com.example.model"
    
    // Annotation style: KOTLINX (default), MOSHI, GSON, or NONE
    annotationStyle = AnnotationStyle.KOTLINX
    
    // Visibility: PUBLIC (default), INTERNAL, or PRIVATE
    visibility = Visibility.PUBLIC
    
    // Root directory for JSON samples (default: src/main/json)
    sourceDir = file("src/main/json")
}
```

## Inference Logic

- **Primitives**: `String`, `Int`, `Long`, `Double`, `Boolean` are inferred directly.
- **Nullability**: If a field is missing or explicitly `null` in any sample in a set, the property is marked as nullable (`?`) and given a default value of `null`.
- **Collections**: JSON arrays are mapped to `List<T>`.
- **Shape Unification**: Objects with overlapping or compatible keys are merged into a single `data class`.
- **Name Mapping**: `snake_case` or `kebab-case` keys are converted to `camelCase` for Kotlin property names. Original names are preserved using the appropriate serialization annotation (e.g., `@SerialName`).

## Build Lifecycle

The `generateJ2kAuto` task runs before Kotlin compilation. It is:
- **Cacheable**: Supports Gradle Build Cache.
- **Incremental**: Only regenerates if JSON samples or configuration changes.
- **Configuration Cache Safe**: Compatible with modern Gradle features.

## Target Audience
Senior Android and Kotlin developers looking to automate DTO management.
