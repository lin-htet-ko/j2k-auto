# Getting Started

## Prerequisites

- Gradle (any recent version compatible with your Kotlin toolchain)
- Either:
    - An **Android** module using **AGP 9.x** (per build variant), or
    - A **Kotlin/JVM** module

## Install from Maven Central

j2k-auto is published to
[Maven Central](https://central.sonatype.com/artifact/io.github.lin-htet-ko/j2k-auto)
as `io.github.lin-htet-ko:j2k-auto`.

Ensure `mavenCentral()` (and `gradlePluginPortal()`) are available to
`pluginManagement`:

```kotlin
// settings.gradle.kts
pluginManagement {
    repositories {
        gradlePluginPortal()
        google()
        mavenCentral()
    }
}
```

Then apply the plugin in the Android or Kotlin/JVM module that should
generate models:

```kotlin
// build.gradle.kts
plugins {
    id("io.github.lin-htet-ko.j2k-auto") version "0.0.1"
}
```

## Configure the extension

```kotlin
import dev.linhtetko.j2kauto.AnnotationStyle

j2kAuto {
    packageName = "com.example.model"          // default: generated.j2kauto
    annotationStyle = AnnotationStyle.KOTLINX  // KOTLINX (default) | MOSHI | GSON | NONE
    // visibility = Visibility.PUBLIC          // PUBLIC (default) | INTERNAL | PRIVATE
    source(layout.projectDirectory.dir("src/main/json")) // default when omitted
}
```

## Add your first JSON sample

Place a sample file under the configured source directory (default
`src/main/json`). You can use subdirectories to organize your JSON
files — j2k-auto will mirror this structure in the generated Kotlin
packages:

```json
// src/main/json/auth/user_profile.json
{
  "id": 1,
  "user_name": "lin",
  "orders": [{ "order_id": 1, "note": "x" }, { "order_id": 2 }]
}
```

The example above would generate the `Profile` class in the
`com.example.model.auth` package (assuming the base package is
configured as `com.example.model`).

## Build

Generation runs automatically as part of every build — the same model as
Hilt/KSP codegen: compilation depends on the generate task, so pressing
**Run** or building always compiles against up-to-date classes.

- Android: `generateJsonModelsDebug`, `generateJsonModelsRelease`, ... (one
  task per variant, wired via the AGP Variant API)
- Kotlin/JVM: `generateJsonModels` (registered on `main`)

After editing a JSON sample (rename a key, add a field), just build/run as
usual — no extra command is needed, and CI needs nothing extra.

## Try the samples

The repository ships two runnable samples that both consume the plugin from
source via `pluginManagement { includeBuild("../..") }`:

- **[`samples/jvm-sample`](https://github.com/lin-htet-ko/j2k-auto/tree/main/samples/jvm-sample)**
  — a Kotlin/JVM consumer with a runtime decode check:

  ```bash
  ../../gradlew run
  ```

- **[`samples/android-sample`](https://github.com/lin-htet-ko/j2k-auto/tree/main/samples/android-sample)**
  — an AGP 9.2.1 Compose app that fetches JSONPlaceholder `/users` with
  Retrofit and decodes the response into generated `User`/`Address`/`Company`
  classes, rendered in a `LazyColumn`:

  ```bash
  ../../gradlew :app:assembleDebug
  ```

Next: see [Implementation](implementation.md) for the full DSL reference,
annotation styles, and inference rules.
