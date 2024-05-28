rootProject.name = "compose-example"
enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

pluginManagement {
    val kotlin_repo_url: String? by settings

    repositories {
        google()
        gradlePluginPortal()
        mavenCentral()

        kotlin_repo_url?.also { maven(it) }
    }
}

dependencyResolutionManagement {
    val kotlin_version: String? by settings
    val kotlin_repo_url: String? by settings

    versionCatalogs {
        create("libs") {
            kotlin_version?.let {
                version("kotlin", it)
            }
        }
    }

    repositories {
        google()
        mavenCentral()

        kotlin_repo_url?.also { maven(it) }
    }
}

include(":composeApp")