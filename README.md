# j2k-auto

[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)
[![Maven Central](https://img.shields.io/maven-central/v/io.github.lin-htet-ko/j2k-auto.svg)](https://central.sonatype.com/artifact/io.github.lin-htet-ko/j2k-auto)

A Gradle plugin that generates Kotlin data classes from sample JSON files at build time. No hand-written DTOs, no manual updates — just drop your JSON samples into a directory and compile against the inferred models.

---

## 📖 Documentation

For full features, inference rules, and detailed integration guides, visit our documentation site:
👉 **[https://lin-htet-ko.github.io/j2k-auto/](https://lin-htet-ko.github.io/j2k-auto/)**

---

## 🚀 Quick Start

### 1. Apply the Plugin

In your module's `build.gradle.kts` (Android or Kotlin/JVM):

```kotlin
plugins {
    id("io.github.lin-htet-ko.j2k-auto") version "0.0.1"
}

j2kAuto {
    packageName = "com.example.model"            // default: generated.j2kauto
    // source(layout.projectDirectory.dir("src/main/json"))  // default
}
```

### 2. Add JSON Samples

Place your `.json` files in `src/main/json/`. For example, `user.json`:

```json
{
  "id": 1,
  "name": "Lin Htet Ko",
  "email": "lin@example.com"
}
```

### 3. Build & Use

Run a build. The plugin generates the Kotlin classes and registers them as source. You can immediately use them in your code:

```kotlin
import com.example.model.User

val user = User(id = 1, name = "Lin", email = "lin@example.com")
```

---

## ⚖️ License

```
Copyright 2026 Lin Htet Ko

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```
