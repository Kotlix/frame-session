import org.springframework.boot.gradle.tasks.bundling.BootJar

val protobufProtocVersion: String by project

plugins {
    id("com.google.protobuf")
}

dependencies {
    api("com.google.protobuf:protobuf-java:$protobufProtocVersion")
}

protobuf {
    protoc {
        artifact = "com.google.protobuf:protoc:$protobufProtocVersion"
    }
    generateProtoTasks {
        all().forEach { task ->
            task.builtins {
                java {}
            }
        }
    }
}

tasks.getByName<BootJar>("bootJar") {
    enabled = false
}

tasks.getByName<Jar>("jar") {
    enabled = true
}
