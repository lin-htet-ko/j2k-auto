# Implementation

This page covers the consumer-facing DSL, annotation styles, inference
rules, and per-platform task wiring. For a deep dive into the plugin's
internals (engine, codegen, Gradle wiring classes), see
[`docs/class-reference.md`](https://github.com/lin-htet-ko/j2k-auto/blob/main/docs/class-reference.md)
in the repository.

## The `j2kAuto` extension

```kotlin
import dev.linhtetko.j2kauto.AnnotationStyle

j2kAuto {
    packageName = "com.example.model"            // default: generated.j2kauto
    annotationStyle = AnnotationStyle.KOTLINX     // KOTLINX (default) | MOSHI | GSON | NONE
    visibility = Visibility.PUBLIC               // PUBLIC (default) | INTERNAL | PRIVATE
    source(layout.projectDirectory.dir("src/main/json"))  // default when omitted
    rootClassName("user_profile.json", "Profile") // optional per-file override

    // useVar = true                 // var instead of val (default false)
    // defaultsForNullable = false   // drop `= null` defaults (default true)
    // alwaysAnnotate = true         // annotate every property (default false)
}
```

| Option | Default | Description |
| --- | --- | --- |
| `packageName` | `generated.j2kauto` | Package for the generated Kotlin files |
| `annotationStyle` | `AnnotationStyle.KOTLINX` | Which serialization annotations to emit |
| `visibility` | `Visibility.PUBLIC` | Visibility modifier for generated classes |
| `source(dir)` | `src/main/json` | Directory scanned for `.json` samples |
| `rootClassName(file, name)` | inferred from filename | Overrides the generated class name for a specific file — required when a file's root is a JSON **array**, since there's no filename-derived singular name |
| `useVar` | `false` | Emit `var` properties instead of `val` |
| `defaultsForNullable` | `true` | Emit `= null` defaults for nullable properties |
| `alwaysAnnotate` | `false` | Annotate every property, even when the serialized name already matches the Kotlin name |

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

## Usage examples

### JVM — runtime decode

From [`samples/jvm-sample`](https://github.com/lin-htet-ko/j2k-auto/tree/main/samples/jvm-sample):

```kotlin
import kotlinx.serialization.json.Json
import sample.model.Profile

fun main() {
    val json = """
        {
          "id": 7,
          "user_name": "milo",
          "is_active": false,
          "home_address": { "street_name": "Raffles Place", "postal_code": "048616" },
          "orders": [ { "order_id": 1, "total": 3.5 } ]
        }
    """.trimIndent()

    val profile = Json.decodeFromString<Profile>(json)
    check(profile.userName == "milo")
    check(profile.orders.single().note == null)
}
```

### Android — Retrofit + generated models

From [`samples/android-sample`](https://github.com/lin-htet-ko/j2k-auto/tree/main/samples/android-sample).

The `build.gradle.kts` configures a root-array file with `rootClassName`,
since a JSON array has no filename-derived singular class name:

```kotlin
j2kAuto {
    packageName = "dev.linhtetko.j2kauto.sample.model"
    annotationStyle = AnnotationStyle.KOTLINX
    source(layout.projectDirectory.dir("src/main/json"))
    // users.json has a root ARRAY — the generated class models one element, so name it User.
    rootClassName("users.json", "User")
}
```

The generated `User` class is then used directly as a Retrofit response type:

```kotlin
import dev.linhtetko.j2kauto.sample.model.User
import retrofit2.http.GET

/**
 * The response type is the j2k-auto-GENERATED [User] class: the plugin wrote
 * the @Serializable DTOs at build time from src/main/json/users.json, and the
 * kotlinx-serialization converter decodes the live response into them here.
 */
interface UsersApi {
    @GET("users")
    suspend fun users(): List<User>
}
```

A `LazyColumn` in `MainActivity` renders the decoded list, and
`CatalogRepository` shows an offline decode of `product_catalog.json` using
the same generated-model pattern.

## Not yet supported

- Kotlin Multiplatform (KMP) source sets
- Date/UUID string sniffing
- Sealed unions for conflicting types

See [Change Logs](changelog.md) for the current release status, or
[Contribution](contributing.md) to help build these out.
