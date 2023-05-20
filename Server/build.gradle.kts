plugins {
    kotlin("jvm") version "1.8.0"
    application
    kotlin("plugin.serialization") version "1.8.0"
    id("com.github.johnrengelman.shadow") version "7.0.0"
}

group = "org.example"
version = "1.0-SNAPSHOT"

dependencies {
    testImplementation(kotlin("test"))
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.5.0")
    implementation("org.apache.logging.log4j:log4j-core:2.20.0")
    implementation ("com.charleskorn.kaml:kaml:0.51.0")
    implementation("io.mockk:mockk:1.13.4")
    implementation("org.postgresql:postgresql:42.3.8")
    implementation(project(":Common"))
    // JWT
    implementation("io.jsonwebtoken:jjwt-api:0.11.5")
    testImplementation("org.junit.jupiter:junit-jupiter:5.8.1")
    testImplementation("org.junit.jupiter:junit-jupiter:5.8.1")
    runtimeOnly("io.jsonwebtoken:jjwt-impl:0.11.5")
    implementation("io.jsonwebtoken:jjwt-jackson:0.11.5") // or 'io.jsonwebtoken:jjwt-gson:0.11.5' for gson
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(11)
}

application {
    mainClass.set("ServerKt")
}
