# Change Logs

All notable changes to j2k-auto are documented on this page. The format is
loosely based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project follows [Semantic Versioning](https://semver.org/).

## 1.0.0-alpha

### Added

- Support for **Multiple Targets**: You can now define multiple `targets { register("name") { ... } }` in the `j2kAuto` block.
    - Each target can have its own `packageName`, `source`, and overrides for all other configuration options.
    - Targets inherit top-level configuration as conventions.
    - Separate generation tasks and output directories per target to prevent class name collisions.
- **Automatic Subpackage Mirroring**: The plugin now automatically reflects the directory structure of your JSON source files in the generated Kotlin packages.
    - Cross-package type resolution: Models in different subpackages can now reference each other correctly with automatic imports.
    - Updated `rootClassName` DSL to support relative paths for files in subdirectories.
- Support for **Configurable Visibility**: You can now set the visibility modifier of generated data classes via the `visibility` option in the `j2kAuto` block.
    - Supported visibility modifiers: `PUBLIC` (default), `INTERNAL`, and `PRIVATE`.

---

## 0.0.1 — Initial release

### Added

- Gradle plugin (`io.github.lin-htet-ko.j2k-auto`) that generates Kotlin
  `data class` models from sample `.json` files at build time.
- Support for **Android** projects (AGP 9.x, wired per build variant via the
  Variant API) and **Kotlin/JVM** projects.
- `j2kAuto { }` DSL: `packageName`, `annotationStyle`, `source(dir)`,
  `rootClassName(file, name)`, `useVar`, `defaultsForNullable`,
  `alwaysAnnotate`.
- Four annotation styles: `KOTLINX`, `MOSHI`, `GSON`, `NONE`.
- Schema inference engine: nested object → nested class, array → `List<T>`,
  `null` → nullable, numeric widening (`Int` → `Long` → `Double`).
- Shape merging across array elements and shape **unification** across
  files (compatible shapes collapse into one class; incompatible ones stay
  separate).
- `snake_case` / `kebab-case` → `camelCase` conversion, with the original
  key preserved via `@SerialName` / `@Json(name = ...)` / `@SerializedName`.
- Cacheable, incremental-friendly, configuration-cache safe generation task.
- `jvm-sample` and `android-sample` sample projects demonstrating real
  consumption (Retrofit + kotlinx-serialization).

### Notes

- Published to
  [Maven Central](https://central.sonatype.com/artifact/io.github.lin-htet-ko/j2k-auto)
  as `io.github.lin-htet-ko:j2k-auto:0.0.1`. See
  [Getting Started](getting-started.md).
- **Not yet supported:** Kotlin Multiplatform (KMP) source sets, date/UUID
  string sniffing, sealed unions for conflicting types.

---

Future releases will be appended above this line as they ship.
