# Contribution

Contributions to j2k-auto are welcome — bug reports, feature requests, docs
improvements, and pull requests.

## Project layout

```
src/main/kotlin/dev/linhtetko/j2kauto/
├── AnnotationStyle.kt   # public DSL enum
├── engine/              # pure JSON -> schema logic (no Gradle, no KotlinPoet)
│                        # SchemaInferrer, TypeMerger, ShapeUnifier, SchemaRegistry, Names
├── codegen/             # schema -> Kotlin source via KotlinPoet
│                        # J2kPipeline, KotlinFileGenerator, annotation strategies, CodegenOptions
└── gradle/              # Gradle integration
                         # J2kAutoPlugin, J2kAutoExtension, GenerateKotlinFromJsonTask,
                         # AndroidWiring, KotlinJvmWiring
```

The dependency direction is strict: `gradle → codegen → engine`. The engine
knows nothing about Gradle or KotlinPoet, which keeps it fully unit-testable
with plain JUnit.

## Local development

```bash
git clone https://github.com/lin-htet-ko/j2k-auto.git
cd j2k-auto

./gradlew test            # engine unit tests + codegen golden tests
./gradlew functionalTest  # Gradle TestKit: JVM, Android, up-to-date, build-cache, config-cache
./gradlew publishToMavenLocal
```

The samples under `samples/j2kautoandroidsample` consume
the plugin from source (`pluginManagement { includeBuild("../..") }`), so
they're a good way to exercise real end-to-end behavior while developing.

## What to test when you change something

- **Engine changes** (`engine/` — inference, merging, unification, naming):
  add or update unit tests under `src/test`. Favor golden-file style tests
  for codegen output.
- **Codegen changes** (`codegen/` — KotlinPoet generation, annotation
  strategies): update the golden tests so generated output is reviewed in
  the diff.
- **Gradle wiring changes** (`gradle/` — plugin, extension, task, Android/JVM
  wiring): run `./gradlew functionalTest`, which exercises the plugin via
  Gradle TestKit against JVM and Android projects, including up-to-date
  checks, build cache, and configuration cache.

## Opening a pull request

1. Fork the repository and create a feature branch.
2. Make your change with accompanying tests (see above).
3. Run `./gradlew test functionalTest` locally before opening the PR.
4. Describe the change and link any related issue in the PR description.

## License

j2k-auto is licensed under the
[Apache License 2.0](https://github.com/lin-htet-ko/j2k-auto/blob/main/LICENSE).
By contributing, you agree that your contributions will be licensed under
the same license.
