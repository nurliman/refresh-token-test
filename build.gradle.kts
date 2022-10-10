import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

val ktorVersion: String by project

plugins {
    kotlin("jvm") version "1.7.10"
    kotlin("plugin.serialization") version "1.7.10"
    id("com.google.devtools.ksp") version "1.7.10-1.0.6"
    application
}

group = "dev.nurliman"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(kotlin("test"))

    // okhttp
    implementation("com.squareup.okhttp3:okhttp:4.10.0")

    // kotlinx-serialization-json
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.4.0")

    // ktor
    implementation("io.ktor:ktor-client-core:$ktorVersion")
    implementation("io.ktor:ktor-client-okhttp:$ktorVersion")
    implementation("io.ktor:ktor-client-content-negotiation:$ktorVersion")
    implementation("io.ktor:ktor-client-auth:$ktorVersion")
    implementation("io.ktor:ktor-serialization-kotlinx-json:$ktorVersion")

    // koin
    implementation("io.insert-koin:koin-core:3.2.2")
    implementation("io.insert-koin:koin-annotations:1.0.3")
    ksp ("io.insert-koin:koin-ksp-compiler:1.0.3")
}

tasks.test {
    useJUnitPlatform()
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}

// for KSP
sourceSets.main {
    java.srcDirs("build/generated/ksp/main/kotlin")
}

application {
    mainClass.set("MainKt")
}