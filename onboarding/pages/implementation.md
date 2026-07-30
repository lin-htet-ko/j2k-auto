# Implementation

This page covers the consumer-facing DSL, annotation styles, inference
rules, and per-platform task wiring.

## The `j2kAuto` extension

```kotlin
import dev.linhtetko.j2kauto.AnnotationStyle

j2kAuto {
    packageName = "com.example.store.models"     // default: generated.j2kauto
    annotationStyle = AnnotationStyle.KOTLINX     // KOTLINX (default) | MOSHI | GSON | NONE
    visibility = Visibility.PUBLIC               // PUBLIC (default) | INTERNAL | PRIVATE
    source(layout.projectDirectory.dir("src/main/json/catalog"))  // default when omitted
    rootClassName("product_details.json", "Product") // optional per-file override

    // useVar = true                 // var instead of val (default false)
    // defaultsForNullable = false   // drop `= null` defaults (default true)
    // alwaysAnnotate = true         // annotate every property (default false)

    // Multiple targets support (e.g., separating Remote API and Local Cache models)
    targets {
        register("api") {
            packageName = "com.example.store.remote.dtos"
            source(layout.projectDirectory.dir("src/main/json/remote"))
        }
        register("local") {
            packageName = "com.example.store.local.entities"
            source(layout.projectDirectory.dir("src/main/json/local"))
            visibility = Visibility.INTERNAL
        }
    }
}
```

| Option | Default | Description |
| --- | --- | --- |
| `packageName` | `generated.j2kauto` | Package for the generated Kotlin files |
| `annotationStyle` | `AnnotationStyle.KOTLINX` | Which serialization annotations to emit |
| `visibility` | `Visibility.PUBLIC` | Visibility modifier for generated classes |
| `source(dir)` | `src/main/json` | Directory scanned for `.json` samples |
| `rootClassName(file, name)` | inferred from filename | Overrides the generated class name for a specific file |
| `useVar` | `false` | Emit `var` properties instead of `val` |
| `defaultsForNullable` | `true` | Emit `= null` defaults for nullable properties |
| `alwaysAnnotate` | `false` | Annotate every property |
| `targets { ... }` | N/A | Register additional generation targets with unique packages/sources |

### How Multiple Targets Work

Each registered target in the `targets` block:
1. Inherits all top-level properties (like `annotationStyle` or `useVar`) as defaults.
2. Can override any of these properties individually.
3. Generates code into its own separate output directory to prevent class name collisions.
4. Registers its output as a source directory for the project.

The top-level `packageName` and `source(...)` are treated as the "default" target. If you only need one package, you can keep using the top-level configuration without the `targets` block.

## Automatic Subpackage Mirroring

j2k-auto automatically mirrors your directory structure in the generated
Kotlin packages. If your base package is `com.example.model` and you have:

- `src/main/json/user.json` → `com.example.model.User`
- `src/main/json/auth/login.json` → `com.example.model.auth.Login`
- `src/main/json/data/dtos/product.json` → `com.example.model.data.dtos.Product`

This keeps your models organized without needing to register separate
targets for every subdirectory.

> [!NOTE]
> If you use `rootClassName` overrides for files in subdirectories, you must
> provide the relative path as the key:
> `rootClassName("auth/login.json", "LoginRequest")`

## Annotation styles

`AnnotationStyle` controls what gets emitted on generated classes:

| Style | Emits |
| --- | --- |
| `KOTLINX` (default) | `@Serializable`, `@SerialName(...)` for renamed properties |
| `MOSHI` | `@JsonClass(generateAdapter = true)`, `@Json(name = ...)` |
| `GSON` | `@SerializedName(...)` |
| `NONE` | Plain data classes with no annotations |

Unknown or conflicting shapes fall back to `JsonElement` (`KOTLINX`) or
`Any?` (other styles).

## Visibility options

`Visibility` controls the visibility modifier of generated data classes:

| Visibility | Modifier |
| --- | --- |
| `PUBLIC` (default) | `public` |
| `INTERNAL` | `internal` |
| `PRIVATE` | `private` |

Note: `PRIVATE` classes are generated as top-level private classes in their respective files.

## Inference rules

- Nested JSON objects become nested `data class`es; arrays become `List<T>`;
  `null` values become nullable types.
- Scalar types are detected as `Int` / `Long` / `Double` / `Boolean` /
  `String`, with `Int` widening to `Long`/`Double` on conflict.
- Object shapes across **array elements are merged**: a field missing or
  null in some elements becomes nullable in the generated class.
- Compatible object shapes — same keys, compatible types — **unify into one
  class**, both within a file and across files (nullability is OR-ed,
  numeric types widen). Incompatible same-key shapes (e.g. `id: Int` vs.
  `id: String`) stay as separate classes. Name collisions get a
  parent-prefixed name.
- `snake_case` / `kebab-case` keys become idiomatic `camelCase` properties,
  with the original key preserved via `@SerialName` / `@Json(name = ...)` /
  `@SerializedName`, depending on the configured `annotationStyle`.

### Example

Given `src/main/json/user_profile.json`:

```json
{
  "id": 1,
  "user_name": "lin",
  "orders": [{ "order_id": 1, "note": "x" }, { "order_id": 2 }]
}
```

With `AnnotationStyle.KOTLINX`, j2k-auto generates:

```kotlin
@Serializable
public data class Profile(
  public val id: Int,
  @SerialName("user_name")
  public val userName: String,
  public val orders: List<Order>,
)

@Serializable
public data class Order(
  @SerialName("order_id")
  public val orderId: Int,
  public val note: String? = null, // missing in some array elements -> nullable
)
```

## Generated tasks

Generation is wired into the normal build so compiled code always sees
up-to-date models — the same model as Hilt/KSP:

- **Android** (via the AGP Variant API) — one task per build variant, e.g.
  `generateJsonModelsDebug`, `generateJsonModelsRelease`.
- **Kotlin/JVM** — a single `generateJsonModels` task registered on `main`.

The generated directory is registered as a Kotlin source directory
automatically, and the task output is cacheable and configuration-cache
safe.

See [Samples](samples.md) for a real-world project example using Retrofit and
generated models.

## Not yet supported

- Kotlin Multiplatform (KMP) source sets
- Date/UUID string sniffing
- Sealed unions for conflicting types

See [Change Logs](changelog.md) for the current release status, or
[Contribution](contributing.md) to help build these out.
