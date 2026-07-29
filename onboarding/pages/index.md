![j2k-auto logo](assets/j2k-auto-logo.png)

## Introduction

Drop a `.json` sample into a directory, apply the plugin, and compile against
the inferred, annotation-ready models — no hand-written DTOs.

<ul class="j2k-meta">
<li><strong>Plugin ID:</strong> <code>io.github.lin-htet-ko.j2k-auto</code></li>
<li><strong>Artifact:</strong> <a href="https://central.sonatype.com/artifact/io.github.lin-htet-ko/j2k-auto"><code>io.github.lin-htet-ko:j2k-auto:0.0.1</code></a></li>
<li><strong>License:</strong> <a href="https://github.com/lin-htet-ko/j2k-auto/blob/main/LICENSE">Apache 2.0</a></li>
</ul>

Many common DTO chores are handled automatically by j2k-auto:

- Nested objects become nested `data class`es; arrays become `List<T>`
- Missing / null fields become nullable properties
- `snake_case` / `kebab-case` keys become idiomatic `camelCase`, with the
  original name preserved via `@SerialName` / `@Json` / `@SerializedName`
- Compatible shapes unify across files into a single class
- Generation runs as part of every build (Hilt/KSP-style) — cacheable and
  configuration-cache safe

j2k-auto works with **Android** (AGP 9.x, per build variant) and plain
**Kotlin/JVM** projects, and can emit annotations for
[kotlinx.serialization](https://github.com/Kotlin/kotlinx.serialization),
[Moshi](https://github.com/square/moshi), [Gson](https://github.com/google/gson),
or none at all.

## Quick start

Apply the plugin and configure the `j2kAuto` extension:

```kotlin
// build.gradle.kts
import dev.linhtetko.j2kauto.AnnotationStyle

plugins {
    id("io.github.lin-htet-ko.j2k-auto") version "0.0.1"
}

j2kAuto {
    packageName = "com.example.model"
    annotationStyle = AnnotationStyle.KOTLINX
}
```

Drop a sample into `src/main/json/user_profile.json`:

```json
{
  "id": 1,
  "user_name": "lin",
  "orders": [{ "order_id": 1, "note": "x" }, { "order_id": 2 }]
}
```

Build, and j2k-auto generates:

```kotlin
@Serializable
public data class UserProfile(
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

Continue to [Getting Started](getting-started.md) to wire the plugin into a
real project, or jump to [Implementation](implementation.md) for the full
DSL and inference reference.

## License

```text
Copyright 2026 Lin Htet Ko

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

   https://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```
