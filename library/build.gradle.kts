plugins {
    kotlin("jvm") version "1.9.24"
    `maven-publish`
    `signing`
}

group = "io.github.cdsap"
version = "0.1"

dependencies {
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.4")
    api("io.github.cdsap:geapi-data:0.2.6")
    implementation("org.slf4j:slf4j-simple:1.6.1")
    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}

configure<JavaPluginExtension> {
    withJavadocJar()
    withSourcesJar()
}

publishing {
    repositories {
        maven {
            name = "Snapshots"
            url = uri("https://s01.oss.sonatype.org/content/repositories/snapshots/")

            credentials {
                username = System.getenv("USERNAME_SNAPSHOT")
                password = System.getenv("PASSWORD_SNAPSHOT")
            }
        }
        maven {
            name = "Release"
            url = uri("https://s01.oss.sonatype.org/service/local/staging/deploy/maven2/")

            credentials {
                username = System.getenv("USERNAME_SNAPSHOT")
                password = System.getenv("PASSWORD_SNAPSHOT")
            }
        }
    }
    publications {
        create<MavenPublication>("libPublication") {
            from(components["java"])
            artifactId = "artifacttransform"
            versionMapping {
                usage("java-api") {
                    fromResolutionOf("runtimeClasspath")
                }
                usage("java-runtime") {
                    fromResolutionResult()
                }
            }
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

