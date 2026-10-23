@file:Suppress("UnstableApiUsage")

rootProject.name = "compose-example"
enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

pluginManagement {
    repositories {
        google()
        gradlePluginPortal()
        mavenCentral()
    }
}

dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
    }
}

include(":androidApp")
include(":desktopApp")
include(":webApp")
include(":shared")