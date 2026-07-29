plugins {
    `java-gradle-plugin`
    alias(libs.plugins.kotlin.jvm)
    `maven-publish`
    signing
    alias(libs.plugins.vanniktech.maven.publish)
}

group = "io.github.lin-htet-ko"
version = "0.0.1"

kotlin {
    jvmToolchain(17)
}

val functionalTest: SourceSet = sourceSets.create("functionalTest")

configurations[functionalTest.implementationConfigurationName]
    .extendsFrom(configurations.testImplementation.get())
configurations[functionalTest.runtimeOnlyConfigurationName]
    .extendsFrom(configurations.testRuntimeOnly.get())

dependencies {
    implementation(libs.kotlinpoet)
    implementation(libs.kotlinx.serialization.json)

    // Wiring-only dependencies: consumers bring their own AGP/KGP.
    compileOnly(libs.agp.api)
    compileOnly(libs.kotlin.gradle.plugin)

    testImplementation(libs.junit.jupiter)
    testImplementation(libs.kotlin.test)
    testRuntimeOnly(libs.junit.platform.launcher)

    "functionalTestImplementation"(gradleTestKit())
}

gradlePlugin {
    plugins.create("j2kAuto") {
        id = "io.github.lin-htet-ko.j2k-auto"
        implementationClass = "dev.linhtetko.j2kauto.gradle.J2kAutoPlugin"
        displayName = "j2k-auto"
        description = "Generates Kotlin data classes from JSON sample files at build time."
    }
    testSourceSets(functionalTest)
}

// Functional tests consume the plugin from a build-local Maven repo by ID —
// production-identical classloading (withPluginClasspath isolates the plugin
// from the AGP/KGP classes it needs to wire against).
val functionalTestRepo = layout.buildDirectory.dir("functional-test-repo")

publishing {
    repositories {
        maven {
            name = "FunctionalTest"
            url = uri(functionalTestRepo)
        }
    }
}

// Maven Central coordinates + POM metadata (see README "Publishing" section
// for the one-time Sonatype Central Portal account/namespace/GPG setup).
mavenPublishing {
    publishToMavenCentral()
    signAllPublications()

    coordinates(group.toString(), "j2k-auto", version.toString())

    pom {
        name.set("j2k-auto")
        description.set("Generates Kotlin data classes from JSON sample files at build time")
        inceptionYear.set("2026")
        url.set("https://github.com/lin-htet-ko/j2k-auto")

        licenses {
            license {
                name.set("The Apache License, Version 2.0")
                url.set("https://www.apache.org/licenses/LICENSE-2.0.txt")
                distribution.set("https://www.apache.org/licenses/LICENSE-2.0.txt")
            }
        }
        developers {
            developer {
                id.set("lin-htet-ko")
                name.set("Lin Htet Ko")
                url.set("https://github.com/lin-htet-ko/")
            }
        }
        scm {
            url.set("https://github.com/lin-htet-ko/j2k-auto")
            connection.set("scm:git:git://github.com/lin-htet-ko/j2k-auto.git")
            developerConnection.set("scm:git:ssh://git@github.com/lin-htet-ko/j2k-auto.git")
        }
    }
}

// signAllPublications() ties signing to the "pluginMaven"/marker publications,
// which are also what functionalTest publishes to the local FunctionalTest repo.
// Only *require* signing when a real key is actually configured (i.e. a genuine
// Maven Central release), so `./gradlew check`/`functionalTest` keep working
// without GPG secrets. Real releases must still export a real key beforehand.
signing {
    isRequired = providers.gradleProperty("signing.keyId").isPresent ||
        providers.gradleProperty("signingInMemoryKey").isPresent
}

val functionalTestTask = tasks.register<Test>("functionalTest") {
    description = "Runs Gradle TestKit functional tests."
    group = "verification"
    testClassesDirs = functionalTest.output.classesDirs
    classpath = functionalTest.runtimeClasspath
    dependsOn("publishAllPublicationsToFunctionalTestRepository")
    systemProperty("pluginRepo", functionalTestRepo.get().asFile.absolutePath)
    systemProperty("pluginVersion", version.toString())
}

tasks.named("check") {
    dependsOn(functionalTestTask)
}

tasks.withType<Test>().configureEach {
    useJUnitPlatform()
}
