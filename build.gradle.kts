import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinJvmCompile
import java.net.URI

fun RepositoryHandler.kotlix(repo: String) = maven {
    name = "GitHubPackages"
    url = URI.create("https://maven.pkg.github.com/Kotlix/$repo")
    credentials {
        // picks from: .../user/.gradle/gradle.properties
        username = System.getenv("GITHUB_ACTOR") ?: findProperty("GITHUB_LOGIN") as String?
        password = System.getenv("GITHUB_TOKEN") ?: findProperty("GITHUB_TOKEN") as String?
    }
}

plugins {
    kotlin("jvm") apply false
    kotlin("plugin.spring") apply false
    id("org.springframework.boot") apply false
    id("org.jlleitschuh.gradle.ktlint") apply false
    id("io.spring.dependency-management")
    id("maven-publish")
}

subprojects {
    apply {
        plugin("org.jetbrains.kotlin.jvm")
        plugin("org.jetbrains.kotlin.plugin.spring")
        plugin("org.springframework.boot")
        plugin("io.spring.dependency-management")
        plugin("org.jlleitschuh.gradle.ktlint")
        plugin("maven-publish")
    }

    val springBootVersion: String by project
    val springCloudVersion: String by project

    val groupId: String by project
    val versionIdNumber: String by project
    val versionIdStatus: String by project

    group = groupId
    val versionId: String = if (versionIdStatus.isEmpty()) versionIdNumber else "$versionIdNumber-$versionIdStatus"
    version = versionId

    dependencyManagement {
        imports {
            mavenBom("org.springframework.boot:spring-boot-dependencies:$springBootVersion")
            mavenBom("org.springframework.cloud:spring-cloud-dependencies:$springCloudVersion")
        }
    }

    repositories {
        mavenLocal()
        mavenCentral()

        kotlix("frame-auth")
    }

    publishing {
        publications {
            create<MavenPublication>("maven") {
                this.groupId = groupId
                this.artifactId = project.name
                this.version = versionId
                from(components["java"])
            }
        }
        repositories {
            kotlix("frame-session")
        }
    }
}

tasks.withType<KotlinJvmCompile>().configureEach {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_17)
        freeCompilerArgs.add("-Xjsr305=strict")
    }
}