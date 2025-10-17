plugins {
    java
    `java-library`
    id("com.gradleup.shadow") version "9.2.2"
}

group = "me.mapacheee.gratituderoses"
version = "1.0.0"

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
}

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://oss.sonatype.org/content/repositories/snapshots/")
    maven("https://repo.extendedclip.com/releases/")
    maven("https://maven.enginehub.org/repo/")
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:1.21-R0.1-SNAPSHOT")

    api("com.thewinterframework:paper:1.0.4")
    annotationProcessor("com.thewinterframework:paper:1.0.4")

    api("com.thewinterframework:configuration:1.0.1")
    annotationProcessor("com.thewinterframework:configuration:1.0.1")

    api("com.thewinterframework:command:1.0.1")
    annotationProcessor("com.thewinterframework:command:1.0.1")

    implementation("com.zaxxer:HikariCP:5.1.0")
    implementation("net.kyori:adventure-text-serializer-legacy:4.17.0")

    runtimeOnly("org.xerial:sqlite-jdbc:3.46.0.0")
    runtimeOnly("com.mysql:mysql-connector-j:8.4.0")

    compileOnly("me.clip:placeholderapi:2.11.6")
    compileOnly("com.sk89q.worldguard:worldguard-bukkit:7.0.14")
}

tasks.processResources {
    filteringCharset = "UTF-8"
    filesMatching("paper-plugin.yml") {
        expand(
            "name" to project.name,
            "version" to project.version,
            "description" to project.description
        )
    }
}

tasks.shadowJar {
    archiveClassifier.set("")
    exclude("META-INF/*.SF", "META-INF/*.DSA", "META-INF/*.RSA")
    relocate("com.zaxxer.hikari", "me.mapacheee.gratituderoses.libs.hikari")
    dependencies {
        exclude(dependency("org.xerial:sqlite-jdbc:.*"))
        exclude(dependency("com.mysql:mysql-connector-j:.*"))
    }
}

tasks.build {
    dependsOn(tasks.shadowJar)
}
