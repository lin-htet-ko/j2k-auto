# Change Logs

All notable changes to j2k-auto are documented on this page. The format is
loosely based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project follows [Semantic Versioning](https://semver.org/).

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
