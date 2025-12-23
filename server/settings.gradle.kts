rootProject.name = "kuken"

pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
    }
}

dependencyResolutionManagement {
    repositories {
        maven("https://maven.pkg.github.com/devnatan/docker-kotlin") {
            credentials {
                username = providers.environmentVariable("GITHUB_ACTOR")
                    .orElse(providers.gradleProperty("githubActor"))
                    .get()

                password = providers.environmentVariable("GITHUB_TOKEN")
                    .orElse(providers.gradleProperty("githubToken"))
                    .get()
            }

            exclusiveContent {
                forRepository {
                    maven("https://maven.pkg.github.com/devnatan/docker-kotlin")
                }

                filter {
                    includeModule("me.devnatan", "docker-kotlin")
                }
            }
        }

        mavenCentral()
    }
}
