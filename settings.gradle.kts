rootProject.name = "frame-session"

pluginManagement {
    plugins {
        val jvmPluginVersion: String by settings
        val springBootVersion: String by settings
        val springDependencyManagementVersion: String by settings
        val ktlintVersion: String by settings

        kotlin("jvm") version jvmPluginVersion
        kotlin("plugin.spring") version jvmPluginVersion
        id("org.springframework.boot") version springBootVersion
        id("io.spring.dependency-management") version springDependencyManagementVersion
        id("org.jlleitschuh.gradle.ktlint") version ktlintVersion
    }

    repositories {
        mavenCentral()
        gradlePluginPortal()
    }
}

include("frame-session-api")
include("frame-session-client")
include("frame-session-client-starter")
include("frame-session-server")