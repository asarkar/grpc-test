pluginManagement {
    val kotlinPluginVersion: String by settings
    val dokkaPluginVersion: String by settings
    val ktlintVersion: String by settings
    val nexusPluginVersion: String by settings
    plugins {
        kotlin("jvm") version kotlinPluginVersion
        id("org.jetbrains.dokka") version dokkaPluginVersion
        id("org.jlleitschuh.gradle.ktlint") version ktlintVersion
        id("io.github.gradle-nexus.publish-plugin") version nexusPluginVersion
    }
}

rootProject.name = "grpc-test"
