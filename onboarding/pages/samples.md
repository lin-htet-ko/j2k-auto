# Samples

This page showcases the sample projects provided in the `j2k-auto` repository. These samples demonstrate real-world integration of the generated models with popular libraries like Retrofit, Coil, and Jetpack Compose.

## Android Sample (`j2kautoandroidsample`)

The **[`samples/j2kautoandroidsample`](https://github.com/lin-htet-ko/j2k-auto/tree/main/samples/j2kautoandroidsample)** is a full-featured Android application using AGP 9.x and Jetpack Compose.

### Features

- **Retrofit Integration**: Fetches real data from APIs and decodes them directly into j2k-auto generated classes using `kotlinx-serialization`.
- **Multi-Category Data**: Demonstrates organizing JSON samples into subdirectories (`feeds/`, `browse/`), which are automatically mirrored as Kotlin packages.
    - **Feeds**: `Post` and `Comment` models.
    - **Browse**: `Product` and `Recipe` models.
- **Image Loading**: Uses [Coil](https://coil-kt.github.io/coil/) to render images from URLs present in the generated models.
- **Navigation**: Uses Jetpack Navigation to move between different lists of data.

### Configuration

The sample consumes the plugin from the local source via `includeBuild` in `settings.gradle.kts`. In the `app` module, it's configured as follows:

```kotlin
// samples/j2kautoandroidsample/app/build.gradle.kts
j2kAuto {
    packageName = "com.linhtetko.j2k_auto_android_sample.data.model"
    source(layout.projectDirectory.dir("src/main/j2k-auto"))
    annotationStyle = AnnotationStyle.KOTLINX
}
```

### Running the Sample

To build and run the sample, use the Gradle wrapper from the project root:

```bash
./gradlew :samples:j2kautoandroidsample:app:assembleDebug
```

## How to explore

1.  Open the project in Android Studio.
2.  Navigate to `samples/j2kautoandroidsample/app/src/main/j2k-auto/` to see the JSON source files.
3.  Navigate to `samples/j2kautoandroidsample/app/src/main/java/` to see how the generated models are consumed in Repositories and ViewModels.
4.  Build the project to trigger the `generateJsonModelsDebug` task and explore the generated code in `build/generated/j2kauto/`.
