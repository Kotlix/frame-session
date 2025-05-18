import org.springframework.boot.gradle.tasks.bundling.BootJar

dependencies {
    implementation(project(":frame-session-api-kafka"))
    implementation(project(":frame-session-api-proto"))

    implementation("ru.kotlix:frame-auth-client-starter")
    implementation("ru.kotlix:frame-parties-client-starter")
    implementation("ru.kotlix:frame-state-client-starter")

    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310")
    implementation("io.netty:netty-all")

    implementation("org.springframework:spring-context-support")
    implementation("org.springframework.kafka:spring-kafka")
    implementation("org.liquibase:liquibase-core")

    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.jetbrains.kotlin:kotlin-stdlib")

    runtimeOnly("org.postgresql:postgresql")
}

tasks.getByName<BootJar>("bootJar") {
    enabled = true
}

tasks.getByName<Jar>("jar") {
    enabled = false
}

tasks.withType<PublishToMavenRepository> {
    enabled = false
}
