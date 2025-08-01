plugins {
    kotlin("jvm") version "2.2.0"
    `maven-publish`
    `signing`
    id("com.vanniktech.maven.publish") version "0.34.0"
}

group = "io.github.cdsap"
version = "0.1"

dependencies {
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.9.0")
    api("io.github.cdsap:geapi-data:0.3.3")
    implementation("org.slf4j:slf4j-simple:2.0.17")
    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}

configure<JavaPluginExtension> {
    withJavadocJar()
    withSourcesJar()
}

mavenPublishing {
    publishToMavenCentral()
    signAllPublications()
    coordinates("io.github.cdsap", "artifacttransform", "0.1")

    pom {
        scm {
            connection.set("scm:git:git://github.com/cdsap/ArtifactTransformReport/")
            url.set("https://github.com/cdsap/ArtifactTransformReport/")
        }
        name.set("artifacttransform")
        url.set("https://github.com/cdsap/ArtifactTransformReport/")
        description.set(
            "Retrieve Artifact Transforms from Develocity"
        )
        licenses {
            license {
                name.set("The MIT License (MIT)")
                url.set("https://opensource.org/licenses/MIT")
                distribution.set("repo")
            }
        }
        developers {
            developer {
                id.set("cdsap")
                name.set("Inaki Villar")
            }
        }
    }
}


if (extra.has("signing.keyId")) {
    afterEvaluate {
        configure<SigningExtension> {
            (
                extensions.getByName("publishing") as
                    PublishingExtension
                ).publications.forEach {
                    sign(it)
                }
        }
    }
}

