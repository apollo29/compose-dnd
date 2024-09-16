plugins {
    `maven-publish`
}

publishing {
    // Configure all publications
    publications.withType<MavenPublication> {
        // Stub javadoc.jar artifact
        artifact(
            tasks.register("${name}JavadocJar", Jar::class) {
                archiveClassifier.set("javadoc")
                archiveAppendix.set(this@withType.name)
            },
        )

        // Provide artifacts information required by Maven Central
        pom {
            name.set("Compose Drag and Drop")
            description.set("A library that allows you to easily add drag and drop functionality to your Jetpack Compose or Compose Multiplatform projects.")
            url.set("https://github.com/MohamedRejeb/compose-dnd")

            licenses {
                license {
                    name.set("Apache-2.0")
                    url.set("https://opensource.org/licenses/Apache-2.0")
                }
            }
            developers {
                developer {
                    id.set("MohamedRejeb")
                    name.set("Mohamed Rejeb")
                    email.set("mohamedrejeb445@gmail.com")
                }
                developer {
                    id.set("tdascoli")
                    name.set("Thomas D'Ascoli")
                    email.set("thomas@dasco.li")
                }
            }
            issueManagement {
                system.set("Github")
                url.set("https://github.com/MohamedRejeb/compose-dnd/issues")
            }
            scm {
                connection.set("https://github.com/MohamedRejeb/compose-dnd.git")
                url.set("https://github.com/MohamedRejeb/compose-dnd")
            }
        }
    }
}
