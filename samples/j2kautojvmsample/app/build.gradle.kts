import dev.linhtetko.j2kauto.AnnotationStyle

plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.serialization)
    id("io.github.lin-htet-ko.j2k-auto")
    application
}

application {
    mainClass.set("com.linhtetko.j2k_auto_jvm_sample.MainKt")
}

j2kAuto {
    packageName = "com.linhtetko.j2k_auto_jvm_sample.data.model"
    source(layout.projectDirectory.dir("src/main/j2k-auto"))
    annotationStyle = AnnotationStyle.KOTLINX
}

dependencies {
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.retrofit)
    implementation(libs.retrofit.serialization)
    implementation(libs.okhttp)
    implementation(libs.okhttp.logging)
}
