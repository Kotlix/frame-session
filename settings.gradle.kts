rootProject.name = "frame-session"

pluginManagement {
    plugins {
        val jvmPluginVersion: String by settings
        val springBootVersion: String by settings
        val springDependencyManagementVersion: String by settings
        val ktlintVersion: String by settings
        val protobufVersion: String by settings
        val js2pojoVersion: String by settings

        kotlin("jvm") version jvmPluginVersion
        kotlin("plugin.spring") version jvmPluginVersion
        id("org.springframework.boot") version springBootVersion
        id("io.spring.dependency-management") version springDependencyManagementVersion
        id("org.jlleitschuh.gradle.ktlint") version ktlintVersion
        id("com.google.protobuf") version protobufVersion
        id("org.jsonschema2pojo") version js2pojoVersion
    }

    repositories {
        mavenCentral()
        gradlePluginPortal()
    }
}

include("frame-session-api-kafka")
include("frame-session-api-proto")
include("frame-session-server")
