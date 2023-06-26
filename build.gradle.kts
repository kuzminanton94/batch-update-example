plugins {
    kotlin("jvm") version "1.8.21"
    application
}

group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation("ch.qos.logback:logback-classic:1.4.7")

    implementation("org.postgresql:postgresql:42.6.0")
    implementation("com.zaxxer:HikariCP:5.0.1")

    implementation("org.jetbrains.exposed", "exposed-core", "0.41.1")
    implementation("org.jetbrains.exposed", "exposed-dao", "0.41.1")
    implementation("org.jetbrains.exposed", "exposed-jdbc", "0.41.1")

    testImplementation("org.testcontainers:testcontainers:1.18.3")
    testImplementation("org.testcontainers:postgresql:1.18.3")
    testImplementation("org.testcontainers:junit-jupiter:1.18.3")

    testImplementation("org.junit.jupiter:junit-jupiter-params:5.9.3")

    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(17)
}

application {
    mainClass.set("MainKt")
}
